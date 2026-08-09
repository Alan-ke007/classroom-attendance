package com.classroom.attendance.modules.file.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.file.dto.FileUploadResponse;
import com.classroom.attendance.modules.file.entity.FileRecord;
import com.classroom.attendance.modules.file.service.FileRecordService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController extends BaseController {

    private final FileRecordService fileRecordService;

    @RequireRole({"admin", "teacher", "student"})
    @PostMapping("/upload")
    public Result<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "general") String category) {
        FileRecord record = fileRecordService.uploadFile(file, category, currentUserIdOrNull());
        return Result.success("上传成功", FileUploadResponse.builder()
                .id(record.getId()).originalName(record.getOriginalName())
                .fileSize(record.getFileSize()).mimeType(record.getMimeType())
                .category(record.getCategory()).createTime(record.getCreateTime()).build());
    }

    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) {
        fileRecordService.downloadFile(id, response);
    }

    @GetMapping("/{id}")
    public Result<FileRecord> getFile(@PathVariable Long id) {
        return Result.success(fileRecordService.getFileById(id));
    }

    @GetMapping("/list")
    public Result<Page<FileRecord>> getFileList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category) {
        return Result.success(fileRecordService.getFileList(pageNum, pageSize, category));
    }

    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteFile(@PathVariable Long id) {
        fileRecordService.deleteFile(id);
        return Result.success("文件删除成功");
    }
}
