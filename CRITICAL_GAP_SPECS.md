# 智课考勤系统 — 关键缺口功能规格与接口草案 v0.1

> 本文是 `FUNCTIONAL_ANALYSIS_SCENARIO.md` 中 **7 个真实刚需缺口**里优先级最高的 2 个的落地草案:
> **① 人脸防代签(签到核验)** 与 **② 课表对接教务系统**。
> 这两个是"做了就质的飞跃,不做就只是又一个考勤 Demo"的关键能力,也是 YOLOv8 卖点真正落地的闭环。
> 其余 5 个缺口(状态细分/调补课/辅导员角色/申诉/预警规则)按同模板可后续补充。

---

## 缺口 ①:人脸防代签(签到实名核验)

### 1.1 背景与场景
- 纯二维码签到无法防止**代签**:张三把签到码截图发寝室群,李四远程帮扫,系统记张三"已到"但人不在。这是高校考勤头号痛点。
- YOLOv8 已有"人脸检测"能力(mAP@50 85.9%),但当前**只用于行为分析,没接入签到闭环**。本缺口把它延伸到"1:N 人脸比对 + 活体",形成防代签闭环。

### 1.2 功能规格
| 项 | 说明 |
|----|------|
| 签到安全等级 | 教师发起时可选:**L1 纯二维码**(低) / **L2 二维码+人脸**(中,默认) / **L3 固定摄像头无感人脸**(高,需教室装摄像头) |
| 人脸采集 | 学生扫码后在小程序/Web 端调起摄像头采集一张人脸图 |
| 1:N 比对 | 算法服务对"本签到会话所属班级人脸库"做比对,返回最匹配 studentId + 置信度 score |
| 活体检测 | 防照片/屏幕翻拍:眨眼动作 / 反光检测 / 随机指令(转头),失败则拒绝 |
| 核验结果 | 通过 → 记出勤(verified=true);不通过 → 标记需复核(verified=false),教师可手动确认或判代签 |
| 降级 | 摄像头不可用/弱网 → 自动降级 L1,并打标"未人脸核验",由教师事后抽查 |
| 隐私合规 | 首次使用弹《行为监控与人脸采集授权书》;人脸特征加密存储、设留存期限、可删除 |

### 1.3 核心流程(时序)
```
教师端 ──发起签到(选安全等级L2)──> 后端:创建签到会话 Session(SID, classId, 有效时间窗)
学生端 ──扫码(SID)───────────────> 后端:返回会话信息
学生端 ──采集人脸图──────────────> 算法服务 /verify {image, SID}
算法服务 ──1:N比对+活体──────────> 返回 {matchedStudentId, score, livePass}
后端   ──校验 studentId==当前登录用户?──> 写 attendance(status=正常, verified=true, method=face, score)
        └─ 不符/低分 ────────────> attendance(verified=false, needReview=true) + 推教师预警
教师端 ──复核列表 ────────────────> 确认/判代签/改状态
```

### 1.4 数据模型增量
```sql
-- attendance 表增加
ALTER TABLE attendance ADD COLUMN verified       TINYINT   DEFAULT 0;   -- 是否人脸核验通过
ALTER TABLE attendance ADD COLUMN verify_method  VARCHAR(16);          -- qr / qr+face / camera
ALTER TABLE attendance ADD COLUMN verify_score   DECIMAL(5,4);         -- 比对置信度
ALTER TABLE attendance ADD COLUMN need_review    TINYINT   DEFAULT 0;   -- 待复核(疑似代签)

-- 人脸特征表(新增)
CREATE TABLE face_feature (
  id            BIGINT PRIMARY KEY,
  student_id    BIGINT NOT NULL,
  embedding     BLOB,                              -- 人脸特征向量(加密)
  source        VARCHAR(16),                       -- enroll(入学采集)/manual(教师补录)
  created_at    DATETIME,
  expire_at     DATETIME,                          -- 留存期限
  FOREIGN KEY (student_id) REFERENCES student(id)
);
```

### 1.5 接口草案
```
POST /api/attendance/session
  body: { courseId, classId, mode: "QR"|"QR_FACE"|"CAMERA", validMinutes }
  resp: { sessionId, qrPayload, expireAt }

POST /api/attendance/verify
  body: { sessionId, imageBase64 }        // 学生端采集
  resp: { matchedStudentId, score, livePass, status }

GET  /api/attendance/session/:id/review
  resp: { list: [ {attendanceId, student, score, needReview} ] }   // 教师复核列表

POST /api/attendance/:id/review
  body: { action: "confirm"|"reject_proxy"|"fix_status", status? }

# 算法服务(FastAPI 升级后)
POST /algorithm/verify
  body: { session_id, image_base64 }
  resp: { matched_student_id, score, live_pass, boxes }
```

### 1.6 技术要点与风险
- **人脸库构建**:首次需采集(入学/教师补录),这是上线前一次性工程;缺图学生走 L1 并提醒补录。
- **误识(FAR)/拒识(FRR)**:设阈值(如 score≥0.85 通过),并提供教师复核兜底,不追求 100%。
- **并发**:多教室同时签到,算法服务需无状态水平扩展 + 按 classId 分库比对。
- **隐私**:必须授权告知 + 特征加密 + 留存期限 + 可删除,否则有合规风险(这是真实校园项目的硬约束)。
- **降级**:弱网/无摄像头时不能阻断正常签到,自动降 L1 并标记。

---

## 缺口 ②:课表对接教务系统

### 2.1 背景与场景
- 真实高校的课程/班级/教室数据**都在教务系统**(正方/URP/青果等)里,教师课表由教务排定。
- 当前系统靠**手动建课程班级**,极易错且与真实课表不同步 → 考勤数据建立在错误基础上。
- 目标:把课表**导入/同步**进本系统,作为考勤的"事实基准"。

### 2.2 功能规格
| 项 | 说明 |
|----|------|
| 数据源 | 现实多数教务**不开放 API**,主路径是**Excel/CSV 导出导入**;少数提供**只读 DB 视图/中间库**;极少数有 API |
| 导入方式 | 管理员上传教务导出文件 → 系统解析 → **字段映射配置**(教务列↔本系统字段)→ 预览冲突 → 确认写入 |
| 定时同步 | 支持按周/按日增量同步(仅 API/DB 视图类数据源可用),手动导入类只能全量重导 |
| 字段映射 | 课程名/课程号、教师/工号、班级、教室、星期、节次、起止周次 → 映射本系统 course/class |
| 冲突检测 | 同教室同节次冲突、同教师时间冲突、字段缺失 → 导入前预览,人工确认 |
| 幂等与版本 | 以 external_id 做 upsert;保留导入日志与历史版本,可回滚 |
| 手动修正 | 导入后允许教师/管理员微调(调课/补课),并标记来源=manual 不被下次同步覆盖 |

### 2.3 核心流程
```
管理员 ──配置数据源(上传文件 / 填 API 地址)──> 系统解析表头
管理员 ──字段映射(教务列↔本字段)────────────> 保存映射模板(可复用)
系统   ──预览(冲突/缺失高亮)────────────────> 管理员确认
系统   ──upsert 写入 course/class───────────> 记 timetable_import_log(版本/时间/操作人)
定时任务 ──(API/DB类)增量同步 ──────────────> 更新 external_id 命中记录, manual 来源跳过
```

### 2.4 数据模型增量
```sql
-- course 表增加
ALTER TABLE course ADD COLUMN source       VARCHAR(16) DEFAULT 'manual'; -- manual/import/sync
ALTER TABLE course ADD COLUMN external_id  VARCHAR(64);                   -- 教务课程号
ALTER TABLE course ADD COLUMN sync_at      DATETIME;

-- 新增导入日志
CREATE TABLE timetable_import_log (
  id          BIGINT PRIMARY KEY,
  source_type VARCHAR(16),        -- excel/api/db
  mapping_ver VARCHAR(16),
  rows_total  INT,
  rows_ok     INT,
  rows_conflict INT,
  operator    BIGINT,
  created_at  DATETIME
);
```

### 2.5 接口草案
```
POST /api/admin/timetable/import
  body: multipart file (excel/csv) + mappingTemplateId
  resp: { previewId, rowsTotal, conflicts: [...] }

POST /api/admin/timetable/confirm
  body: { previewId, resolve: { rowId: "keepImport"|"keepExisting" } }
  resp: { imported, skipped }

POST /api/admin/timetable/sync        // 仅 API/DB 数据源
  resp: { synced, failed }

GET  /api/admin/timetable/logs
  resp: { list: [ {version, time, operator, rowsOk, conflicts} ] }
```

### 2.6 技术要点与风险
- **异构教务**:正方/URP/青果表结构各异 → 映射**配置化**(mapping 模板),不为某一家写死解析。
- **周次/节次→时间戳**:教务用"起止周次+星期+节次",需换算成本系统可查询的上课时间,做统一 calendar 服务。
- **冲突处理**:导入前必须预览而非静默覆盖,避免清掉手动修正数据(manual 来源保护)。
- **隐私**:课表含师生工号/学号,同步走内网/加密,权限最小集。
- **现实预期**:很多学校**不给 API**,所以"Excel 导入 + 映射模板"是首版必须做扎实的主路径,API 同步作为进阶。

---

## 面试视角(如何使用这两份规格)
- **防代签**讲"我看到纯二维码解决不了代签这个真实痛点,于是把已有的 YOLOv8 人脸能力从行为分析延伸到签到 1:N 比对,并设计了活体+教师复核兜底+隐私合规"——体现"用已有资产解决真实问题"。
- **课表对接**讲"我发现手动建课表是数据错误的根因,于是做了配置化映射+冲突预览+幂等回滚,并基于'多数教务不开放 API'的现实选了 Excel 导入为主路径"——体现"不写死、尊重现实约束、有数据治理意识"。
- 这两段都比"我做了 9 个模块"更有技术深度和产品判断,面试官会追着问 → 你就有得讲。

---

*配套文档:`FUNCTIONAL_ANALYSIS_SCENARIO.md`(场景→缺口总览)、`NEW_SYSTEM_REQUIREMENTS_BLUEPRINT.md`(需求基线)、`SYSTEM_REVIEW_CONFLICT_MAP.md`、`INTERVIEW_CHALLENGES.md`。*
