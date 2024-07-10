package com.xuecheng.content.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseService;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, CourseBase> implements CourseService {

}
