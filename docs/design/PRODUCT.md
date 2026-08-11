---
register: product
---

# 智课考勤 (SmartClass Attendance)

## Product Purpose
智慧课堂考勤管理平台，提供基于 AI 行为分析的课堂考勤、学生管理、数据可视化一站式解决方案。面向高校教师和管理员，实现课堂自动化管理。

## Users
- **管理员 (admin)**: 系统全局管理，用户管理、数据查看、系统配置
- **教师 (teacher)**: 日常考勤管理、学生行为监控、课堂质量评估、数据统计
- **学生 (student)**: 查看个人考勤记录、课程表、行为表现、每周报告

## Brand
- **Tone**: 科技感、专业、简洁、高效 — 类似 Apple 设计语言
- **Visual direction**: 深色模式优先，玻璃拟态卡片，粒子动效背景，霓虹光晕点缀
- **Anti-reference**: 不要厚重的阴影和渐变，不要圆角过大的元素，不要过于花哨的动画
- **Design principles**: 清晰的信息层级、克制的动效、一致的主题变量、可访问性优先

## Strategic constraints
- Built with Vue 3 + Element Plus + CSS custom properties
- All colors use `var(--c-*)` CSS variables for theme switching
- Glass morphism via `backdrop-filter: blur()`
- Existing sci-fi theme with scan lines and glow effects
