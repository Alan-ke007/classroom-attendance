package com.classroom.attendance.modules.face.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 人脸特征库（PII，仅后端可读写；算法服务零 PII 持久化）。
 *
 * <p>对应建表脚本：src/main/resources/sql/face_embedding.sql
 * 表结构（PRD §2.1）：
 * <pre>
 *   student_id    BIGINT          主键（学号即主键，INPUT 写入）
 *   embedding     VARBINARY(2048) 512×float32 小端 = 2048 字节
 *   face_count    TINYINT         建档用到的有效图片数
 *   source        VARCHAR(20)     enroll / re_enroll / mock
 *   model_version VARCHAR(32)     产生特征的模型标识（如 buffalo_l）
 *   image_ref     VARCHAR(255)    NF5 留证引用（默认仅存特征不存原图）
 *   created_at / updated_at       时间戳
 *   deleted       INT(1)          逻辑删除，对齐 MyBatis-Plus 全局配置
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("face_embedding")
public class FaceEmbedding implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "student_id", type = IdType.INPUT) // 学号即主键，INPUT 由业务写入
    private Long studentId;

    private byte[] embedding;        // VARBINARY(2048)，512×float32 LE
    private Integer faceCount;
    private String source;           // enroll / re_enroll / mock
    private String modelVersion;     // buffalo_l
    private String imageRef;         // NF5 留证引用，默认仅存特征

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
