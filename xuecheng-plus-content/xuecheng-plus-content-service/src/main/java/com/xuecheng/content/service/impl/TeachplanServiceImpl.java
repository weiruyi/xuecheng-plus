package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeachplanServiceImpl implements TeachplanService {

    private final TeachplanMapper teachplanMapper;
    private final TeachplanMediaMapper teachplanMediaMapper;

    /**
     * 查询课程计划树形结构
     * @param courseId
     * @return
     */
    public List<TeachplanDto> getTreeNode(Long courseId){
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);

        Map<Long, TeachplanDto> dtoMap = teachplanDtos.stream().collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        List<TeachplanDto> teachplanDtoList = new ArrayList<>();
        teachplanDtos.stream().forEach(item -> {
            if(item.getParentid() == 0){
                teachplanDtoList.add(item);
            }
            TeachplanDto teachplanDto = dtoMap.get(item.getParentid());
            if(teachplanDto != null){
                if(teachplanDto.getTeachPlanTreeNodes() == null){
                    teachplanDto.setTeachPlanTreeNodes(new ArrayList<TeachplanDto>());
                }
                teachplanDto.getTeachPlanTreeNodes().add(item);
            }
        });

        return teachplanDtoList;
    }

    /**
     * 新增或者修改课程计划
     * @param teachplanDto  课程计划信息
     */
    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //1、取出课程计划id
        Long id = teachplanDto.getId();
        //2、如果已经存在就修改
        if(id != null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
            return;
        }
        //3、不存在，插入
        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplanDto, teachplanNew);
        //计算orderby,获取当前课程计划相同parentId的课程数量
        Integer count = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
        teachplanNew.setOrderby(count + 1);

        //插入数据库
        teachplanMapper.insert(teachplanNew);

    }

    /**
     * 根据id删除课程计划
     * @param id
     */
    @Override
    @Transactional
    public void deleteTeachplan(Long id) {
        //1、判断是否是大章节
        Teachplan teachplan = teachplanMapper.selectById(id);
        if(teachplan.getParentid() == 0 && teachplan.getGrade() == 1){
            //2、大章节
            //2.1、如果还有子信息则无法删除
            //2.1.1 统计以当前计划为父计划的计划数
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, id);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            //2.2 如果不为0则无法删除
            if(count > 0){
                XueChengPlusException exception = new XueChengPlusException("课程计划信息还有子级信息，无法操作");
                throw  exception;
            }
            //2.3、没有小章节，直接删除
            teachplanMapper.deleteById(id);
        }
        //3、小章节
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId, id);
        TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
        if(teachplanMedia != null){
            teachplanMediaMapper.deleteById(teachplanMedia.getId());
        }
        teachplanMapper.deleteById(id);

        //4、修改大于的编号
        LambdaQueryWrapper<Teachplan> teachplanLambdaWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaWrapper.gt(Teachplan::getOrderby, teachplan.getOrderby());
        teachplanLambdaWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
        teachplanLambdaWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        List<Teachplan> teachplanList = teachplanMapper.selectList(teachplanLambdaWrapper);

        //所有编号减1
        if(teachplanList != null && teachplanList.size() > 0){
            teachplanList.stream().forEach(item -> {
                item.setOrderby(item.getOrderby() - 1);
                teachplanMapper.updateById(item);
            });
        }

    }

    /**
     * 根据courseId和parentId查询课程数量
     * @param courseId
     * @param parentId
     * @return
     */
    private Integer getTeachplanCount(Long courseId, Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }

    /**
     * 课程计划向上或者向下移动
     * @param id
     * @param flag true为向上移动，false向下移动
     */
    public void changeOrderBy(Long id, Boolean flag){
        //1、查询出当前课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);

        //2、查询出和当前课程计划相同课程，相同级别的，相邻的课程计划
        //向上移动就-1，向下移动+1
        Integer teachplanOrderBy = teachplan.getOrderby();
        Integer nearOrderBy = flag ? teachplanOrderBy - 1 : teachplanOrderBy + 1;

        //查询相邻的课程计划
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
        queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        queryWrapper.eq(Teachplan::getOrderby, nearOrderBy);
        Teachplan nearTeachplan = teachplanMapper.selectOne(queryWrapper);

        //3、如果相邻的课程计划存在就交换
        if(nearTeachplan != null){
            teachplan.setOrderby(nearOrderBy);
            teachplanMapper.updateById(teachplan);

            nearTeachplan.setOrderby(teachplanOrderBy);
            teachplanMapper.updateById(nearTeachplan);
        }
    }
}
