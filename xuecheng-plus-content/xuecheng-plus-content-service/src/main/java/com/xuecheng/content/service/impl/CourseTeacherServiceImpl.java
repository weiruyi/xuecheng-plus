package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseTeacherServiceImpl implements CourseTeacherService {

    private final CourseTeacherMapper courseTeacherMapper;

    /**
     * 根据课程id查询教师列表
     * @param courseId
     * @return
     */
    public List<CourseTeacher> getCourseTeacherListByCourseId(Long courseId){
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(queryWrapper);
        return courseTeachers;
    }

    /**
     * 新增教师
     * @param addCourseTeacherDto
     * @return
     */
    public CourseTeacher addCourseTeacher(AddCourseTeacherDto addCourseTeacherDto){

        if(addCourseTeacherDto.getCourseId() == null){
            XueChengPlusException.cast("课程id不能为空");
        }

        CourseTeacher courseTeacher = new CourseTeacher();
        BeanUtils.copyProperties(addCourseTeacherDto, courseTeacher);
        courseTeacher.setCreateDate(LocalDateTime.now());

        courseTeacherMapper.insert(courseTeacher);

        return courseTeacher;
    }

    /**
     * 修改教师信息
     * @param courseTeacher
     * @return
     */
    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher){
        if(courseTeacher.getId() == null){
            XueChengPlusException.cast("id不能为空");
        }
        if(courseTeacher.getCourseId() == null){
            XueChengPlusException.cast("课程id不能为空");
        }
        if(courseTeacher.getTeacherName() == null){
            XueChengPlusException.cast("教师姓名不能为空");
        }

        courseTeacherMapper.updateById(courseTeacher);
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    /**
     * 删除教师信息
     * @param courseId
     * @param id
     */
    public void deleteCourseTeacher(Long courseId, Long id){
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId, id);
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);

        courseTeacherMapper.delete(queryWrapper);
    }
}
