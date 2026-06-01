package com.classroom.attendance.modules.announcement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.announcement.entity.Announcement;
import com.classroom.attendance.modules.announcement.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController extends BaseController {

    private final AnnouncementService announcementService;

    @GetMapping("/list")
    public Result<Page<Announcement>> getList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(announcementService.getList(pageNum, pageSize));
    }

    @GetMapping("/active")
    public Result<List<Announcement>> getActiveList() {
        return Result.success(announcementService.getActiveList());
    }

    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }

    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> create(@RequestBody Announcement announcement) {
        announcementService.create(announcement);
        return Result.success("发布公告成功");
    }

    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcementService.update(id, announcement);
        return Result.success("更新公告成功");
    }

    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success("删除公告成功");
    }
}
