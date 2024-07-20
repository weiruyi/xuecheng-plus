package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {

    /**
     * 根据课程id查询教师列表
     * @param courseId
     * @return
     */
    public List<CourseTeacher> getCourseTeacherListByCourseId(Long courseId);

    /**
     * 新增教师
     * @param addCourseTeacherDto
     * @return
     */
    public CourseTeacher addCourseTeacher(AddCourseTeacherDto addCourseTeacherDto);


    /**
     * 修改教师信息
     * @param courseTeacher
     * @return
     */
    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 删除教师信息
     * @param courseId
     * @param id
     */
    public void deleteCourseTeacher(Long courseId, Long id);
}
