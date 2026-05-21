package com.classroom.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA路由转发 — 将所有非API路径转发至 index.html，
 * 由前端路由自身处理
 */
@Controller
public class SpaController {

    @RequestMapping(value = {"/", "/student/**", "/teacher/**", "/dashboard/**", "/login/**"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
