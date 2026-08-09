package com.classroom.attendance.modules.attendance.enums;

/**
 * 人脸核验结果状态。存于 attendance.face_status（字符串列）。
 * VERIFIED=后端 extract+余弦比对通过；NEED_REVIEW=算法不可达降级放行；
 * REJECTED=未建档或比对不通过（不写签到）。
 */
public enum FaceStatus {
    VERIFIED,
    NEED_REVIEW,
    REJECTED
}
