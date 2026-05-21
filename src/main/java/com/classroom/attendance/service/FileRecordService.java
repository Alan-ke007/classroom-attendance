package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.model.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 文件记录服务接口
 */
public interface FileRecordService {

    /**
     * 上传文件
     * @param file 待上传的文件
     * @param category 文件分类
     * @param uploadBy 上传者ID
     * @return 文件记录
     */
    FileRecord uploadFile(MultipartFile file, String category, Long uploadBy);

    /**
     * 下载文件
     * @param fileId 文件ID
     * @param response HTTP响应
     */
    void downloadFile(Long fileId, HttpServletResponse response);

    /**
     * 获取文件记录
     * @param id 文件ID
     * @return 文件记录
     */
    FileRecord getFileById(Long id);

    /**
     * 分页查询文件列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param category 分类筛选（可选）
     * @return 分页结果
     */
    Page<FileRecord> getFileList(Integer pageNum, Integer pageSize, String category);

    /**
     * 删除文件记录（同时删除物理文件）
     * @param id 文件ID
     */
    void deleteFile(Long id);
}
