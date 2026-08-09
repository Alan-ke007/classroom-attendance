package com.classroom.attendance.modules.attendance.controller;

import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.modules.attendance.entity.Attendance;
import com.classroom.attendance.modules.attendance.enums.AttendanceStatus;
import com.classroom.attendance.modules.attendance.mapper.AttendanceMapper;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController extends BaseController {

    private final AttendanceMapper attendanceMapper;

    @RequireRole({"admin", "teacher"}) // H1：全量考勤报表导出属 admin/teacher 专属
    @GetMapping("/pdf/attendance")
    public void exportAttendancePdf(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        try {
            String fileName = "考勤报表_" + LocalDate.now() + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

            OutputStream out = response.getOutputStream();
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            PdfFont font = loadChineseFont();

            document.add(new Paragraph("考勤报表").setFont(font).setFontSize(22).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            document.add(new Paragraph("统计周期：" + start + " 至 " + end).setFont(font).setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(6));
            document.add(new Paragraph("导出时间：" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attendance> w =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            if (startDate != null) w.ge(Attendance::getAttendanceDate, start);
            if (endDate != null) w.le(Attendance::getAttendanceDate, end);
            w.orderByAsc(Attendance::getAttendanceDate);
            List<Attendance> list = attendanceMapper.selectList(w);

            long present = list.stream().filter(a -> AttendanceStatus.PRESENT.equals(a.getStatus())).count();
            long late = list.stream().filter(a -> AttendanceStatus.LATE.equals(a.getStatus())).count();
            int total = list.size();
            double rate = total > 0 ? Math.round((present + late) * 1000.0 / total) / 10.0 : 100.0;

            document.add(new Paragraph("出勤率: " + rate + "%   总记录: " + total + " 条").setFont(font).setFontSize(12).setMarginBottom(16));

            String[] headers = {"ID", "学生ID", "课程ID", "日期", "状态", "签到时间"};
            Table table = new Table(UnitValue.createPercentArray(headers.length)).useAllAvailableWidth();
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setFont(font).setBold().setFontSize(9)));
            }
            for (Attendance a : list) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(a.getId())).setFont(font).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(a.getStudentId())).setFont(font).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(a.getCourseId())).setFont(font).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(a.getAttendanceDate() != null ? a.getAttendanceDate().toString() : "").setFont(font).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(a.getStatus() != null ? a.getStatus().getDescription() : "").setFont(font).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(a.getCheckInTime() != null ? a.getCheckInTime().toLocalTime().toString() : "").setFont(font).setFontSize(8)));
            }
            document.add(table);
            document.add(new Paragraph("\n—— 本报表由智课考勤系统自动生成 ——").setFont(font).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(30).setFontColor(new DeviceRgb(153, 153, 153)));

            document.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error("导出PDF失败", e);
            throw new com.classroom.attendance.infrastructure.exception.BusinessException("导出PDF失败: " + e.getMessage());
        }
    }

    private PdfFont loadChineseFont() {
        try {
            return PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (Exception e) {
            try {
                return PdfFontFactory.createFont("Ming-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            } catch (Exception e2) {
                try {
                    return PdfFontFactory.createFont();
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
    }
}
