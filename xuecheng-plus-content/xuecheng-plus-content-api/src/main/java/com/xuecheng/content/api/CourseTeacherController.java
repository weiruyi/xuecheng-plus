package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.AddCourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/courseTeacher")
@Api(value = "课程教师管理编辑接口")
@RequiredArgsConstructor
public class CourseTeacherController {

    private final CourseTeacherService courseTeacherService;

    @ApiOperation("查询教师")
    @GetMapping("list/{courseId}")
    public List<CourseTeacher> getTeacherListByCourseId(@PathVariable Long courseId){
        List<CourseTeacher> list = courseTeacherService.getCourseTeacherListByCourseId(courseId);
        return list;
    }

    @ApiOperation("新增教师")
    @PostMapping()
    public CourseTeacher addCourseTeacher(@RequestBody @Validated AddCourseTeacherDto addCourseTeacherDto){
        log.info("新增教师：{}",addCourseTeacherDto);
        CourseTeacher courseTeacher = courseTeacherService.addCourseTeacher(addCourseTeacherDto);
        return courseTeacher;
    }

    @ApiOperation("修改教师")
    @PutMapping()
    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacher courseTeacher){
        log.info("修改教师信息：{}",courseTeacher);
        CourseTeacher courseTeacherNew = courseTeacherService.updateCourseTeacher(courseTeacher);
        return courseTeacherNew;
    }

    @ApiOperation("删除教师")
    @DeleteMapping("course/{courseId}/{id}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long id){
        log.info("删除教师信息，课程ID：{},教师ID:{}",courseId, id);
        courseTeacherService.deleteCourseTeacher(courseId, id);
    }
}
