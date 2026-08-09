package com.classroom.attendance.modules.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private Long id;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    private String category;
    private LocalDateTime createTime;
}
