package com.classroom.attendance.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel导出工具类
 */
public class ExcelExportUtil {
    
    /**
     * 导出Excel文件
     * @param headers 表头数组
     * @param data 数据列表，每个元素是一个Map，key为字段名，value为值
     * @param sheetName 工作表名称
     * @return Excel文件的字节数组
     */
    public static byte[] exportExcel(String[] headers, List<Map<String, Object>> data, String sheetName) throws IOException {
        // 创建工作簿
        Workbook workbook = new XSSFWorkbook();
        
        // 创建工作表
        Sheet sheet = workbook.createSheet(sheetName);
        
        // 创建表头样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        // 创建数据样式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 创建表头行
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            // 设置列宽
            sheet.setColumnWidth(i, 4000);
        }
        
        // 填充数据
        if (data != null && !data.isEmpty()) {
            int rowNum = 1;
            for (Map<String, Object> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = row.createCell(i);
                    String key = getHeaderKey(headers[i]);
                    Object value = rowData.get(key);
                    
                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    } else {
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(dataStyle);
                }
            }
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            // 设置最大宽度
            if (sheet.getColumnWidth(i) > 8000) {
                sheet.setColumnWidth(i, 8000);
            }
        }
        
        // 写入字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 根据表头名称获取对应的key
     */
    private static String getHeaderKey(String header) {
        // 这里可以根据实际情况建立表头到key的映射
        Map<String, String> headerToKeyMap = new HashMap<>();
        headerToKeyMap.put("学号", "studentNo");
        headerToKeyMap.put("姓名", "name");
        headerToKeyMap.put("班级", "className");
        headerToKeyMap.put("课程", "courseName");
        headerToKeyMap.put("考勤日期", "attendanceDate");
        headerToKeyMap.put("状态", "status");
        headerToKeyMap.put("签到时间", "checkInTime");
        headerToKeyMap.put("置信度", "confidence");
        headerToKeyMap.put("备注", "remark");
        headerToKeyMap.put("行为类型", "behaviorType");
        headerToKeyMap.put("行为时间", "behaviorTime");
        headerToKeyMap.put("是否处理", "handled");
        headerToKeyMap.put("处理备注", "handleRemark");
        
        return headerToKeyMap.getOrDefault(header, header);
    }
}
