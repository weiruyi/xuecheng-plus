package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDTO;
import com.xuecheng.content.model.po.CourseBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class CourseBaseMapperTests {

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper() {

        CourseBase courseBase = courseBaseMapper.selectById(18L);
        Assertions.assertNotNull(courseBase);
        log.info(courseBase.toString());

        // 分页查询
        // 拼装查询条件
        QueryCourseParamsDTO courseParamsDTO = new QueryCourseParamsDTO();
        courseParamsDTO.setCourseName("java");

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDTO.getCourseName()),CourseBase::getName, courseParamsDTO.getCourseName());
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDTO.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDTO.getAuditStatus());

        //创建分页参数,当前页码，每页记录数
        Page<CourseBase> page = new Page<>(1, 2);
        // 开始进行分页查询
        Page<CourseBase> pageresult = courseBaseMapper.selectPage(page, queryWrapper);

        List<CourseBase> items = pageresult.getRecords();
        long total = pageresult.getTotal();
        long current = pageresult.getCurrent();
        long pages = pageresult.getSize();
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items, total, current, pages);
        log.info(courseBasePageResult.toString());

    }

}
