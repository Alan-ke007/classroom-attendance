package com.classroom.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVO {
    private Long id;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    private String category;
    private LocalDateTime createTime;
}
