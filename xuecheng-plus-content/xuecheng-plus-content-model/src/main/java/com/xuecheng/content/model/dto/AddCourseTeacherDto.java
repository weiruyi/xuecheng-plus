package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Data
@ToString
public class AddCourseTeacherDto {
    private  Long courseId;

    @NotEmpty(message = "教师名不能为空")
    private String teacherName;

    private String position;

    private String introduction;
}
