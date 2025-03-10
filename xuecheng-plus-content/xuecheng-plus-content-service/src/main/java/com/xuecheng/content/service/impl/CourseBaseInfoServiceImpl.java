package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @description 课程信息管理业务接口实现类
 * @author Mr.M
 * @date 2022/9/6 21:45
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class CourseBaseInfoServiceImpl  implements CourseBaseInfoService {


    private final CourseBaseMapper courseBaseMapper;
    private final CourseMarketMapper courseMarketMapper;
    private final CourseCategoryMapper courseCategoryMapper;
    private final TeachplanMapper teachplanMapper;
    private final CourseTeacherMapper courseTeacherMapper;

    /*
     * @description 课程查询接口
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 条件条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(Long companyId, PageParams pageParams, QueryCourseParamsDTO queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        //课程id
        queryWrapper.eq(CourseBase::getCompanyId, companyId);

        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    /**
     * 新增课程
     * @param companyId 机构id
     * @param dto 课程信息
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //参数合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new XueChengPlusException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }

        //1、向课程基本信息表course_base
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto, courseBase);

        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");

        //插入数据库
        int insert = courseBaseMapper.insert(courseBase);
        if(insert < 0){
            throw new RuntimeException("添加课程失败");
        }

        //2、向课程营销表course_market写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        //营销表和基本信息表一对一，靠id连接
        courseMarket.setId(courseBase.getId());
        //添加
        int i = saveCourseMarket(courseMarket);
        if(i < 0){
            throw new RuntimeException("添加课程营销信息失败");
        }

        //3、从数据库查询课程详细信息
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfoById(courseBase.getId());
        return courseBaseInfoDto;

    }


    /**
     * 向营销表中添加数据，有则修改无则添加
     * @param courseMarket
     * @return
     */
    private int saveCourseMarket(CourseMarket courseMarket) {
        //参数校验
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)) {
            throw new XueChengPlusException("收费规则为空");
        }
        if(charge.equals("201001")){
            if(courseMarket.getPrice() == null || courseMarket.getPrice() < 0){
                throw new XueChengPlusException("课程价格不能为空，而且必须大于0");
            }
        }
        
        //从数据库查询营销信息
        Long id = courseMarket.getId();
        CourseMarket courseMarketOld = courseMarketMapper.selectById(id);
        if(courseMarketOld == null){
            //插入
            int insert = courseMarketMapper.insert(courseMarket);
            return insert;
        } else {
            //更新
            BeanUtils.copyProperties(courseMarket, courseMarketOld);
            courseMarketOld.setId(id);
            int i = courseMarketMapper.updateById(courseMarketOld);
            return i;
        }
    }


    /**
     * 从数据库查询课程详细信息
     * @param courseId
     * @return
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfoById(Long courseId) {
        //1、从课程信息表查询课程信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        //2、从课程营销表查询信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //3、组合
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();

        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //查询课程分类名称
        CourseCategory courseCategory1 = courseCategoryMapper.selectById(courseBaseInfoDto.getMt());
        courseBaseInfoDto.setMtName(courseCategory1.getName());
        CourseCategory courseCategory2 = courseCategoryMapper.selectById(courseBaseInfoDto.getSt());
        courseBaseInfoDto.setStName(courseCategory2.getName());

        return courseBaseInfoDto;
    }

    /**
     * 修改课程信息
     * @param companyId
     * @param editCourseDto
     * @return
     */
    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBaseInfo(Long companyId, EditCourseDto editCourseDto) {
        //课程id
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }

        //验证修改的是否是本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        //封装课程基本数据
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        //更新课程基本信息
        courseBaseMapper.updateById(courseBase);

        //封装课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        saveCourseMarket(courseMarket);

        //查询课程基本信息
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfoById(courseId);
        return courseBaseInfoDto;
    }

    /**
     * 删除课程信息
     * @param id
     * @param companyId
     */
    @Override
    @Transactional
    public void deleteCourse(Long id, Long companyId) {
        //课程id
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if(courseBase == null){
            XueChengPlusException.cast("课程不存在");
        }

        //验证修改的是否是本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能删除本机构的课程");
        }

        //课程审核状态为为提交才可以删除
        if(!courseBase.getAuditStatus().equals("202002")){
            XueChengPlusException.cast("课程的审核状态为未提交时方可删除");
        }

        //删除课程需要删除课程相关的基本信息、营销信息、课程计划、课程教师信息。
        //课程基本信息
        courseBaseMapper.deleteById(courseBase.getId());
        //营销信息
        courseMarketMapper.deleteById(courseBase.getId());
        //课程计划
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, id);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);
        //课程教师信息
        LambdaQueryWrapper<CourseTeacher> courseTeacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        courseTeacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, id);
        courseTeacherMapper.delete(courseTeacherLambdaQueryWrapper);

    }


}
