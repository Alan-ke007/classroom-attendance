# 智课考勤 · 设计系统详细规范（Direction A 双轨）v1.0

> **基于**：`NEW_SYSTEM_REQUIREMENTS_BLUEPRINT.md` v1.0 §6 —— 设计方向已选 **A 双轨**（玻璃后台 + 粗野大屏）。
> **角色**：UI Designer 交付给开发的正式设计契约（token 数值 + 双轨组件规格 + 关键界面示意图）。
> **配套**：`DESIGN_SYSTEM_PREVIEW_A.html`（本规范的实时视觉预览，可直接打开核对）。

---

## 1. 设计 Token

### 1.1 共享品牌 Token（两轨共用，单一事实来源）
```
--brand-cyan:    #00E5FF   /* 主品牌霓虹 — 实时/AI/主操作 */
--brand-magenta: #E040FB   /* 次品牌霓虹 — 行为/预警 */
--brand-yellow:  #FFD600   /* 点睛霓虹 — 高亮数字/告警 */
--brand-ink:     #04121A   /* 霓虹底上的深色文字（保证 AA 对比） */
```

### 1.2 轨道 A · 玻璃后台 Token（深色优先）
```
/* 表面与背景 */
--a-bg:          #0A0E1A;            /* 应用底色（深空蓝黑）*/
--a-bg-elev:     #0F1626;            /* 浮层/弹窗底 */
--a-glass:       rgba(255,255,255,.06);   /* 玻璃卡片底 */
--a-glass-strong:rgba(255,255,255,.10);
--a-border:      rgba(255,255,255,.10);   /* 玻璃描边 */
--a-border-strong:rgba(255,255,255,.18);

/* 文字 */
--a-text:        #E6EBF5;            /* 主文字 对比底色 ≈ 14:1 ✓AA */
--a-text-2:      #9AA7C2;            /* 次文字 ≈ 6.5:1 ✓AA */
--a-text-3:      #6B7894;            /* 辅助 ≈ 4.0:1（仅大字/禁用）*/

/* 语义色（已校准深色底对比）*/
--a-primary:     var(--brand-cyan);
--a-success:     #2EE6A6;
--a-warning:     #FFB020;
--a-danger:      #FF5C7A;
--a-info:        #4DA8FF;

/* 圆角 / 阴影 / 模糊 */
--a-radius-sm: 8px;  --a-radius-md: 12px;  --a-radius-lg: 16px;  --a-radius-pill: 999px;
--a-shadow-1: 0 1px 2px rgba(0,0,0,.30);
--a-shadow-2: 0 8px 24px rgba(0,0,0,.40);
--a-blur:     16px;   /* backdrop-filter */
--a-glow:     0 0 0 1px rgba(0,229,255,.30), 0 0 24px rgba(0,229,255,.18); /* 主操作微光 */
```

### 1.3 轨道 B · 粗野大屏 Token（近黑 + 硬边硬影 + 霓虹辉光）
```
--b-bg:          #05070D;            /* 大屏底色（近黑）*/
--b-panel:       #0E1320;            /* 实体面板 */
--b-line:        #1B2740;            /* 分割线 */
/* 粗野边框按类别上色（硬边，无圆角）*/
--b-border-cyan:    2px solid var(--brand-cyan);
--b-border-magenta: 2px solid var(--brand-magenta);
--b-border-yellow:  2px solid var(--brand-yellow);
--b-shadow-hard: 6px 6px 0 #000;     /* 硬偏移黑影（招牌）*/
--b-radius: 0;                        /* 全部尖角 */
/* 辉光（仅大屏，强于后台）*/
--b-glow-cyan:   0 0 12px rgba(0,229,255,.55);
--b-glow-magenta:0 0 12px rgba(224,64,251,.55);
--b-glow-yellow: 0 0 12px rgba(255,214,0,.55);
--b-scan:        rgba(0,229,255,.05); /* 扫描线叠层 */
```

### 1.4 通用 Token（两轨共用）
```
/* 字体 */
--font-sans: 'Inter', system-ui, 'PingFang SC', sans-serif;
--font-mono: 'JetBrains Mono', ui-monospace, monospace;
--font-display: 'Space Grotesk', var(--font-sans);  /* 大标题/数字 */
/* 字阶 12→48 */
--fs-xs:12px --fs-sm:14px --fs-base:16px --fs-lg:18px --fs-xl:20px
--fs-2xl:24px --fs-3xl:30px --fs-4xl:36px --fs-5xl:48px
/* 间距 4 基准 */
--sp-1:4 --sp-2:8 --sp-3:12 --sp-4:16 --sp-6:24 --sp-8:32 --sp-12:48 --sp-16:64
/* 动效 */
--motion-fast:150ms --motion:250ms --motion-slow:400ms  (ease)
/* 断点 640 / 768 / 1024 / 1280 */
/* z-index: toast 1000 / modal 1100 / nav 100 */
```

---

## 2. 组件规格（双轨）

### 2.1 作用域规则（根因治理）
- 路由容器加 `.track-glass` 或 `.track-brutal`，**所有轨道样式仅在该作用域内生效**，撤销旧系统全局 `!important`。
- Token 通过 CSS 变量按作用域切换：` .track-glass { --bg: var(--a-bg); ... }`；`.track-brutal { --bg: var(--b-bg); ... }`。
- 共享组件（Button/Input/Card）读取 `--bg/--border/--radius/--shadow` 等"语义变量"，由作用域决定取值 → 同一组件两轨自动换肤。

### 2.2 按钮 Button（4 态 + loading + 焦点环）
| 变体 | 轨道 A 玻璃 | 轨道 B 粗野 |
|------|------------|------------|
| Primary | cyan 实底 + `--brand-ink` 文字；hover 微光+升 1px；radius 12px | cyan 实底 + 黑字；硬影 6px；hover 影位移至 3px；尖角；大写 mono |
| Secondary | 玻璃描边（`--a-border-strong`）+ 浅字 | 透明底 + 2px 青边 + 青字；硬影 |
| Ghost | 无边框透明，hover 玻璃底 | 无边框，hover 青字辉光 |
| Danger | 红描边/红字，hover 红底 | 红边红字，硬影，hover 红底 |
- **共用**：`:focus-visible` 2px 焦点环（A：cyan；B：黄）；`:disabled` 40% 透明 + `not-allowed`；`:active` 下沉；loading 显示 spinner 并禁重复点击；高度 ≥ 40px（触控 ≥ 44px）。
- **主操作唯一化**：每屏仅 1 个 Primary（如"发起二维码签到"），其余 Secondary/Ghost。

### 2.3 输入框 Input
- A：玻璃底 + 1px 柔和描边，focus 时边框转 cyan + 外发光（无硬影）；placeholder `--a-text-3`。
- B：尖角 + 2px 青边，focus 边框转黄 + 黄辉光；mono 字体。

### 2.4 卡片 Card
- A：玻璃（`--a-glass` + `backdrop-filter:blur(16px)`）+ 圆角 16px + 柔和阴影；hover 轻微上浮。
- B：实体面板 `--b-panel` + 类别硬边（青/品红/黄）+ 硬影 6px；无圆角；可选扫描线叠层。

### 2.5 导航（侧边栏 + 顶栏）
- A：玻璃侧栏（磨砂、圆角项、active 项青色左条 + 微光）；顶栏同玻璃。
- B：**不在大屏使用侧栏**；大屏用顶部硬边标题条 + 大写 mono 分区标签。

### 2.6 表格 Table（数据密集 — 轨道 A 重点）
- A：透明行 + 低对比分隔线；表头 `--a-text-2`；斑马纹用 `--a-glass` 极淡；行 hover 玻璃高亮；状态用 Badge。
- 避免粗野风上表格（旧系统痛点）：表格只走轨道 A，保证可读性。

### 2.7 徽标/标签 Badge
- 状态语义：正常/出勤=success、迟到=warning、缺勤/异常=danger、实时/AI=cyan。
- A：圆角胶囊 + 语义色淡底；B：尖角 + 硬边 + 大写 mono。

### 2.8 对话框 Modal / 提示 Toast
- A：玻璃浮层 + 柔影；B：硬边面板 + 硬影（仅管理端弹窗走 A，大屏告警用顶部硬边 toast 条）。

---

## 3. 关键界面示意图（布局规格）

### 3.1 登录页（Track A 玻璃）
- 居中玻璃卡片（480×auto），深空蓝黑底 + 粒子/极淡光晕背景。
- 标题"智课考勤"display 字体；输入框（账号/密码）玻璃风；Primary"登录"cyan 实底；下方第三方/忘记密码 Ghost。
- 左上预留 logo 位、右下预留占位（呼应你海报留白习惯，但此处为 Web 登录，仅作品牌协调）。

### 3.2 管理后台首页 Dashboard（Track A 玻璃 · Bento）
- 左玻璃侧栏（导航）+ 顶栏（用户/通知/主题）。
- 主区 Bento 网格：4 张统计玻璃卡（出勤率/迟到率/行为异常/请假待批）+ 趋势柱状图卡 + 最近考勤记录表卡 + 右侧"发起二维码签到"Primary 操作区。
- 霓虹仅作点缀（主操作/状态 Badge/图表高亮线）。

### 3.3 考勤记录列表（Track A 玻璃 · Table）
- 筛选条（课程/日期/状态）+ 数据表格（学号/姓名/状态 Badge/时间/核验标记）+ 分页。
- 行内操作：补签(Ghost)、详情(Secondary)；危险操作(删除)走 Danger 二次确认。

### 3.4 行为监控大屏（Track B 粗野）
- 近黑底 + 扫描线；顶部硬边标题条"实时行为监控 · LIVE"（黄辉光）。
- 三栏硬边面板：左"教室画面预览框"（青边）、中"行为分布条"（品红边 + 大写 mono 数字）、右"异常预警列表"（黄边 + 滚动告警）。
- 超大 mono 数字显示"在座率 96%""异常 3"，霓虹辉光拉满；无圆角、硬影、大写。

---

## 4. 无障碍（WCAG 2.1 AA）

| 项 | 规范 |
|----|------|
| 对比度 | 正文 `--a-text`/`--a-text-2` 对 `--a-bg` ≥ 4.5:1（已校）；大屏大字 ≥ 3:1。霓虹底文字一律用 `--brand-ink` 深色，避免浅字失败 |
| 焦点 | 全局 `:focus-visible` 2px 可见环（旧系统被 `!important` 吃掉的修复点）|
| 触控 | 交互元素 ≥ 44px（平板/触屏签到场景）|
| 动效 | 所有动画 `prefers-reduced-motion: reduce` 下降级为瞬时 |
| 语义 | 用原生 `<button>/<table>/<nav>` + ARIA，不滥用 div |

---

## 5. 响应式

- 断点 640/768/1024/1280；Web 桌面优先 + 平板适配（侧栏可收起）。
- 大屏（Track B）按 1080p/2K/4K 流式排版，数字字号随视口 `clamp()`。
- 小程序自适应多机型（独立样式，不套 Web token，但复用组件逻辑）。

---

## 6. 落地实现指引

- **单一来源**：`packages/design-system/tokens.css` 定义全部变量；轨道值用 `.track-glass/.track-brutal` 作用域覆盖。
- **工程化**：UnoCSS `theme` 引用同一套 token；Storybook 承载两套画廊（A/B）做组件文档与视觉回归。
- **迁移**：撤销 `App.vue` 全局 `.el-button--primary !important`，改为作用域；旧 sci-fi 霓虹仅保留给 Track B 组件。
- **验收**：对照本规范 + 预览 HTML 做像素与对比度核对，确保两轨不串味。

---

*本规范与 `DESIGN_SYSTEM_PREVIEW_A.html` 配套；下一阶段可据 §3 示意图直接 scaffold 页面或补全 P0 地基。*
