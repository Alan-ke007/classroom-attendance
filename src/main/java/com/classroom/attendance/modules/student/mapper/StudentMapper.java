package com.classroom.attendance.modules.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.modules.student.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /** 原子累加学风分并在 [min,max] 内 clamp，单条 UPDATE 天然避免并发丢失更新。 */
    @Update("UPDATE student SET credit_score = LEAST(#{max}, GREATEST(#{min}, credit_score + #{delta})) WHERE id = #{id}")
    int updateScoreClamp(@Param("id") Long id, @Param("delta") int delta, @Param("min") int min, @Param("max") int max);

    @Update("UPDATE student SET credit_earned = IFNULL(credit_earned, 0) + #{delta} WHERE id = #{id}")
    int updateCreditEarned(@Param("id") Long id, @Param("delta") int delta);

    @Update("UPDATE student SET credit_deducted = IFNULL(credit_deducted, 0) + #{delta} WHERE id = #{id}")
    int updateCreditDeducted(@Param("id") Long id, @Param("delta") int delta);
}
