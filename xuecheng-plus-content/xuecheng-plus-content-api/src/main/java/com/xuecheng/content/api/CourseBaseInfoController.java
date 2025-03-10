package com.xuecheng.content.api;


import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "课程管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseBaseInfoController {

    private final CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程列表查询")
    @PostMapping("list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list(PageParams pageParams,@RequestBody(required = false) QueryCourseParamsDTO queryCourseParamsDTO){
        //取出用户身份,获取机构id
        String companyId = SecurityUtil.getUser().getCompanyId();
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(Long.parseLong(companyId), pageParams, queryCourseParamsDTO);
        log.info("courseBasePageResult:{}", courseBasePageResult);
        return courseBasePageResult;
    }

    @ApiOperation("新增课程")
    @PostMapping()
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto){
        //TODO:获取用户id
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBaseInfoDto;
    }

    @ApiOperation("根据id查询课程")
    @GetMapping("/{id}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long id){
        log.info("根据id查询课程信息");
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        System.out.println(principal);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user);
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfoById(id);
        return courseBaseInfo;
    }

    @ApiOperation("修改课程信息")
    @PutMapping()
    public CourseBaseInfoDto updateCourseBase(@RequestBody @Validated EditCourseDto editCourseDto){
        log.info("修改课程信息：{}", editCourseDto);
        //TODO：companyId
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBaseInfo(companyId, editCourseDto);
        return courseBaseInfoDto;
    }


    @ApiOperation("删除课程")
    @DeleteMapping("{id}")
    public void deleteCourse(@PathVariable Long id){
        log.info("删除课程，ID：{}",id);
        //TODO：companyId
        Long companyId = 1232141425L;

        courseBaseInfoService.deleteCourse(id, companyId);
    }

}
