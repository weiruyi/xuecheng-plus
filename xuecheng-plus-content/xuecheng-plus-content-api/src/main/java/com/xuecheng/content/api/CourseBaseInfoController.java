package com.xuecheng.content.api;


import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "课程管理")
@RestController
@RequiredArgsConstructor
public class CourseBaseInfoController {

    private final CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams,@RequestBody(required = false) QueryCourseParamsDTO queryCourseParamsDTO){
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDTO);
        log.info("courseBasePageResult:{}", courseBasePageResult);
        return courseBasePageResult;
    }
}
