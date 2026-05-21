package com.classroom.attendance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 课堂考勤系统启动类
 */
@SpringBootApplication
@MapperScan("com.classroom.attendance.mapper")
@EnableScheduling
public class ClassroomAttendanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassroomAttendanceApplication.class, args);
        System.out.println("========================================");
        System.out.println("课堂智能考勤系统启动成功！");
        System.out.println("========================================");
    }
}
