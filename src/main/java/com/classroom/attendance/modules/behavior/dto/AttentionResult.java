package com.classroom.attendance.modules.behavior.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课堂专注度评分结果（智能考勤核心指标）。
 *
 * <p>ATI(Attention Index) = (N_F − α·N_V) / (N_F + N_V + ε) × 100，归一化到 [-100, 100]。
 * 其中 N_F 为专注行为次数（举手/阅读/书写），N_V 为违纪行为次数（玩手机/低头/趴桌），
 * α 为违纪权重（默认 1.2），ε 为平滑项。该指标将 YOLOv8 行为识别结果量化为“考勤质量/专注度”信号，
 * 是论文核心创新点之一。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttentionResult {

    /** 专注度指数，范围 [-100, 100] */
    private double ati;

    /** 等级：优(≥60) / 良(≥40) / 中(≥20) / 差(<20) */
    private String level;

    /** 行为样本总数 */
    private int totalSamples;

    /** 专注行为次数 */
    private int focusCount;

    /** 违纪行为次数 */
    private int violationCount;
}
