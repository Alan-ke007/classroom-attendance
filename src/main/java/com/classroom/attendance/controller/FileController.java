package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.model.FileRecord;
import com.classroom.attendance.service.FileRecordService;
import com.classroom.attendance.vo.FileUploadVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器 — 上传/下载/列表/删除
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileRecordService fileRecordService;

    /**
     * 上传文件（支持头像上传）
     */
    @RequireRole({"admin", "teacher", "student"})
    @PostMapping("/upload")
    public Result<FileUploadVO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "general") String category,
            HttpServletRequest request) {
        log.info("接收到文件上传请求: name={}, size={}, category={}", file.getOriginalFilename(), file.getSize(), category);
        try {
            Object userIdAttr = request.getAttribute("userId");
            Long uploadBy = userIdAttr != null ? ((Number) userIdAttr).longValue() : null;
            FileRecord record = fileRecordService.uploadFile(file, category, uploadBy);
            return Result.success("上传成功", FileUploadVO.builder()
                    .id(record.getId())
                    .originalName(record.getOriginalName())
                    .fileSize(record.getFileSize())
                    .mimeType(record.getMimeType())
                    .category(record.getCategory())
                    .createTime(record.getCreateTime())
                    .build());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) {
        log.info("接收到文件下载请求，ID: {}", id);
        fileRecordService.downloadFile(id, response);
    }

    /**
     * 获取文件记录详情
     */
    @GetMapping("/{id}")
    public Result<FileRecord> getFile(@PathVariable Long id) {
        log.info("接收到查询文件请求，ID: {}", id);
        try {
            FileRecord record = fileRecordService.getFileById(id);
            if (record == null) {
                return Result.error("文件不存在");
            }
            return Result.success(record);
        } catch (Exception e) {
            log.error("查询文件失败", e);
            return Result.error("查询文件失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询文件列表
     */
    @GetMapping("/list")
    public Result<Page<FileRecord>> getFileList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category) {
        log.info("接收到分页查询文件请求，页码: {}, 每页大小: {}, 分类: {}", pageNum, pageSize, category);
        try {
            Page<FileRecord> page = fileRecordService.getFileList(pageNum, pageSize, category);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询文件列表失败", e);
            return Result.error("查询文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteFile(@PathVariable Long id) {
        log.info("接收到删除文件请求，ID: {}", id);
        try {
            fileRecordService.deleteFile(id);
            return Result.success("文件删除成功");
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return Result.error("删除文件失败: " + e.getMessage());
        }
    }
}
