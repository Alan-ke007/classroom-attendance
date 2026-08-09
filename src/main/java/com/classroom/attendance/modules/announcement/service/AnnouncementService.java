package com.classroom.attendance.modules.announcement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.announcement.entity.Announcement;
import com.classroom.attendance.modules.announcement.mapper.AnnouncementMapper;
import com.classroom.attendance.modules.auth.entity.User;
import com.classroom.attendance.modules.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final UserMapper userMapper;

    public Page<Announcement> getList(int pageNum, int pageSize) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        w.orderByDesc(Announcement::getIsPinned).orderByDesc(Announcement::getCreateTime);
        Page<Announcement> result = announcementMapper.selectPage(page, w);
        fillPublisherNames(result.getRecords());
        return result;
    }

    public List<Announcement> getActiveList() {
        String role = SecurityUtil.getCurrentRole();
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        w.and(wrapper -> wrapper.isNull(Announcement::getTargetRole).or().eq(Announcement::getTargetRole, "")
                .or().eq(Announcement::getTargetRole, role));
        w.orderByDesc(Announcement::getIsPinned).orderByDesc(Announcement::getCreateTime);
        w.last("LIMIT 5");
        List<Announcement> list = announcementMapper.selectList(w);
        fillPublisherNames(list);
        return list;
    }

    public Announcement getById(Long id) {
        Announcement a = announcementMapper.selectById(id);
        BusinessException.notNull(a, "公告不存在");
        if (a.getPublisherId() != null) {
            User u = userMapper.selectById(a.getPublisherId());
            if (u != null) a.setPublisherName(u.getRealName());
        }
        return a;
    }

    public Announcement create(Announcement a) {
        a.setPublisherId(SecurityUtil.getCurrentUserId());
        announcementMapper.insert(a);
        return a;
    }

    public Announcement update(Long id, Announcement a) {
        BusinessException.notNull(announcementMapper.selectById(id), "公告不存在");
        a.setId(id);
        announcementMapper.updateById(a);
        return a;
    }

    public void delete(Long id) {
        BusinessException.isTrue(announcementMapper.deleteById(id) > 0, "公告不存在或删除失败");
    }

    private void fillPublisherNames(List<Announcement> list) {
        if (list == null || list.isEmpty()) return;
        var publisherIds = list.stream().map(Announcement::getPublisherId).filter(id -> id != null).distinct().toList();
        if (publisherIds.isEmpty()) return;
        var userMap = userMapper.selectBatchIds(publisherIds).stream()
                .collect(Collectors.toMap(User::getId, User::getRealName));
        list.forEach(a -> {
            if (a.getPublisherId() != null && userMap.containsKey(a.getPublisherId())) {
                a.setPublisherName(userMap.get(a.getPublisherId()));
            }
        });
    }
}
