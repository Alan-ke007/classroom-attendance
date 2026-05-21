package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.FileRecordMapper;
import com.classroom.attendance.model.FileRecord;
import com.classroom.attendance.service.FileRecordService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件记录服务实现类
 */
@Slf4j
@Service
public class FileRecordServiceImpl implements FileRecordService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Autowired
    private FileRecordMapper fileRecordMapper;

    @Override
    public FileRecord uploadFile(MultipartFile file, String category, Long uploadBy) {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unknown";
        }

        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalName.substring(dotIndex).toLowerCase();
        }

        String storedName = UUID.randomUUID().toString() + extension;
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        Path fullDir = Paths.get(uploadPath, dateDir);
        try {
            Files.createDirectories(fullDir);
        } catch (IOException e) {
            throw new RuntimeException("创建存储目录失败", e);
        }

        Path targetPath = fullDir.resolve(storedName);
        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }

        String relativePath = dateDir.replace("\\", "/") + "/" + storedName;

        FileRecord record = FileRecord.builder()
                .originalName(originalName)
                .storedName(storedName)
                .filePath(relativePath)
                .fileExtension(extension.replace(".", ""))
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .uploadBy(uploadBy)
                .category(category != null ? category : "general")
                .build();

        fileRecordMapper.insert(record);
        log.info("文件上传成功: id={}, name={}, path={}", record.getId(), originalName, relativePath);
        return record;
    }

    @Override
    public void downloadFile(Long fileId, HttpServletResponse response) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) {
            throw new RuntimeException("文件不存在");
        }

        Path filePath = Paths.get(uploadPath, record.getFilePath());
        File file = filePath.toFile();
        if (!file.exists()) {
            throw new RuntimeException("文件已丢失");
        }

        try {
            String encodedName = URLEncoder.encode(record.getOriginalName(), "UTF-8")
                    .replace("+", "%20");
            response.setContentType(record.getMimeType() != null ? record.getMimeType() : "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
            response.setContentLengthLong(record.getFileSize());

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                 BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    @Override
    public FileRecord getFileById(Long id) {
        return fileRecordMapper.selectById(id);
    }

    @Override
    public Page<FileRecord> getFileList(Integer pageNum, Integer pageSize, String category) {
        Page<FileRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(FileRecord::getCreateTime);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(FileRecord::getCategory, category);
        }
        return fileRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public void deleteFile(Long id) {
        FileRecord record = fileRecordMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("文件不存在");
        }

        Path filePath = Paths.get(uploadPath, record.getFilePath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除物理文件失败: {}", filePath, e);
        }

        fileRecordMapper.deleteById(id);
        log.info("文件删除成功: id={}", id);
    }
}
