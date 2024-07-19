package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDTO;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseCategoryServiceImpl implements CourseCategoryService {

    private final CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDTO> queryTreeNodes(String id) {
        //1、获取种类集合
        List<CourseCategoryTreeDTO> courseCategoryTreeDTOS = courseCategoryMapper.selectTreeNodes(id);
        //2、将种类集合转换成结果类型
        //2.1 先将集合转换成map
        Map<String, CourseCategoryTreeDTO> treeDTOMap = courseCategoryTreeDTOS.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //2.2 遍历list
        List<CourseCategoryTreeDTO> courseCategoryTree = new ArrayList<>();
        courseCategoryTreeDTOS.stream().filter(item -> !id.equals(item.getId())).forEach(item ->{
            if(item.getParentid().equals(id)){
                courseCategoryTree.add(item);
            }
            CourseCategoryTreeDTO courseCategoryTreeDTO = treeDTOMap.get(item.getParentid());
            if(courseCategoryTreeDTO != null){
                if(courseCategoryTreeDTO.getChildrenTreeNodes() == null){
                    courseCategoryTreeDTO.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDTO>());
                }
                courseCategoryTreeDTO.getChildrenTreeNodes().add(item);
            }
        });
        return courseCategoryTree;
    }
}
