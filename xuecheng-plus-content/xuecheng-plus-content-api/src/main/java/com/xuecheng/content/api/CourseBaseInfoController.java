package com.xuecheng.content.api;


import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "课程管理")
@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
public class CourseBaseInfoController {

    private final CourseService courseService;

    @ApiOperation("课程查询")
    @GetMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,@RequestBody(required = false) QueryCourseParamsDTO queryCourseParamsDTO){
        log.info("course");
        CourseBase courseBase = courseService.getById(1L);
        log.info("courseBase:{}",courseBase);
        return null;
    }
}
