package com.classroom.attendance.infrastructure.response;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> records;
    private long    total;
    private long    pages;
    private long    current;
    private long    size;

    public static <T> PageResult<T> of(Page<T> page) {
        return PageResult.<T>builder()
                .records(page.getRecords())
                .total(page.getTotal())
                .pages(page.getPages())
                .current(page.getCurrent())
                .size(page.getSize())
                .build();
    }
}
