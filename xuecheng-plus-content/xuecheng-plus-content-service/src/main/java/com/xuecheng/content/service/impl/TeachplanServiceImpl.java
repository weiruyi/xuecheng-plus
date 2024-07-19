package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
