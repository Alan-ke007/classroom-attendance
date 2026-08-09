package com.classroom.attendance.modules.face.controller;

import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.face.dto.EnrollRequest;
import com.classroom.attendance.modules.face.dto.FaceCheckinRequest;
import com.classroom.attendance.modules.face.dto.FaceCheckinResult;
import com.classroom.attendance.modules.face.dto.RecognizeRequest;
import com.classroom.attendance.modules.face.dto.RecognizeResult;
import com.classroom.attendance.modules.face.dto.EnrollResult;
import com.classroom.attendance.modules.face.service.FaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 人脸核验接口（学生本人可调用，不加 @RequireRole admin/teacher，避免回归学生自阅被挡）。
 * 身份一律取自 SecurityUtil.getCurrentStudentId()（服务端，防越权/伪造）。
 */
@Slf4j
@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
public class FaceController extends BaseController {

    private final FaceService faceService;

    /**
     * 建档（F3/F6，仅本人）。不添加 @RequireRole，否则学生被挡（P0 拦下的坑）。
     * 请求体 studentId 与服务端不一致 → 403。
     */
    @PostMapping("/enroll")
    public Result<EnrollResult> enroll(@RequestBody EnrollRequest req) {
        return Result.success(faceService.enroll(req.getImages(), req.getStudentId()));
    }

    /**
     * 核验代理（F5，复用/管理端诊断）。返回 {matched, confidence, studentId, reason}；
     * 未建档内联 reason=NO_ENROLLMENT；算法不可达 → 抛 40005（全局异常映射）。
     */
    @PostMapping("/api/algorithm/recognize")
    public Result<RecognizeResult> recognize(@RequestBody RecognizeRequest req) {
        return Result.success(faceService.verify(req.getImage(), req.getStudentId()));
    }

    /**
     * 小程序主链路合并端点（F7/F8）：提取+比对+写签到，后端单一权威判定（R6）。
     * 学生本人可调用，不添加角色限制。
     */
    @PostMapping("/api/attendance/face-checkin")
    public Result<FaceCheckinResult> faceCheckin(@RequestBody FaceCheckinRequest req) {
        return Result.success(faceService.faceCheckin(req.getCourseId(), req.getImage()));
    }
}
