package com.xuecheng.content.api;


import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "课程管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseBaseInfoController {

    private final CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询")
    @PostMapping("list")
    public PageResult<CourseBase> list(PageParams pageParams,@RequestBody(required = false) QueryCourseParamsDTO queryCourseParamsDTO){
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDTO);
        log.info("courseBasePageResult:{}", courseBasePageResult);
        return courseBasePageResult;
    }

    @ApiOperation("新增课程")
    @PostMapping()
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Inseter.class}) AddCourseDto addCourseDto){
        //TODO:获取用户id
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBaseInfoDto;
    }

}
