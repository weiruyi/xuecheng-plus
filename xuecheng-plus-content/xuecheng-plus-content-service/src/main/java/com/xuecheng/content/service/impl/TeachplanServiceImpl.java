package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
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

    private Integer getTeachplanCount(Long courseId, Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}
