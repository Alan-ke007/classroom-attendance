package com.classroom.attendance.controller;

import com.classroom.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.model.Attendance;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ExportController {

    @Autowired
    private AttendanceMapper attendanceMapper;

    @GetMapping("/pdf/attendance")
    public void exportAttendancePdf(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        try {
            String fileName = "考勤报表_" + LocalDate.now() + ".pdf";
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");

            OutputStream out = response.getOutputStream();
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 尝试加载中文字体
            PdfFont font;
            try {
                font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H",
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            } catch (Exception e) {
                try {
                    font = PdfFontFactory.createFont("Ming-Light", "UniGB-UCS2-H",
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                } catch (Exception e2) {
                    font = PdfFontFactory.createFont();
                }
            }

            // 标题
            document.add(new Paragraph("考勤报表")
                    .setFont(font)
                    .setFontSize(22)
                    .setBold()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(20));

            // 日期信息
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
            document.add(new Paragraph("统计周期：" + start + " 至 " + end)
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(6));
            document.add(new Paragraph("导出时间：" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginBottom(20));

            // 查询数据
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attendance> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            if (startDate != null) wrapper.ge(Attendance::getAttendanceDate, start);
            if (endDate != null) wrapper.le(Attendance::getAttendanceDate, end);
            wrapper.orderByAsc(Attendance::getAttendanceDate);
            List<Attendance> list = attendanceMapper.selectList(wrapper);

            // 统计信息
            long present = list.stream().filter(a -> "present".equals(a.getStatus())).count();
            long late = list.stream().filter(a -> "late".equals(a.getStatus())).count();
            long absent = list.stream().filter(a -> "absent".equals(a.getStatus())).count();
            long leave = list.stream().filter(a -> "leave".equals(a.getStatus())).count();
            int total = list.size();
            double rate = total > 0 ? Math.round((present + late) * 1000.0 / total) / 10.0 : 100.0;

            document.add(new Paragraph("出勤率: " + rate + "%   总记录: " + total + " 条")
                    .setFont(font)
                    .setFontSize(12)
                    .setMarginBottom(6));
            document.add(new Paragraph("出勤: " + present + "  迟到: " + late + "  缺勤: " + absent + "  请假: " + leave)
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(16));

            // 明细表格
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
                table.addCell(new Cell().add(new Paragraph(translateStatus(a.getStatus())).setFont(font).setFontSize(8)));
                table.addCell(new Cell().add(new Paragraph(a.getCheckInTime() != null ? a.getCheckInTime().toLocalTime().toString() : "").setFont(font).setFontSize(8)));
            }

            document.add(table);

            // 页脚
            document.add(new Paragraph("\n—— 本报表由智课考勤系统自动生成 ——")
                    .setFont(font)
                    .setFontSize(9)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                    .setMarginTop(30)
                    .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(153, 153, 153)));

            document.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            log.error("导出PDF失败", e);
            throw new RuntimeException("导出PDF失败: " + e.getMessage());
        }
    }

    private String translateStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "present" -> "出勤";
            case "absent" -> "缺勤";
            case "late" -> "迟到";
            case "leave" -> "请假";
            default -> status;
        };
    }
}
