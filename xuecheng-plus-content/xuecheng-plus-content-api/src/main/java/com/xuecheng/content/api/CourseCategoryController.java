package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CourseCategoryTreeDTO;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(tags = "课程种类管理")
@RequiredArgsConstructor
@RestController
public class CourseCategoryController {

    private final CourseCategoryService courseCategoryService;

    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDTO> queryTreeNodes(){

        List<CourseCategoryTreeDTO> courseCategoryTreeDTOS = courseCategoryService.queryTreeNodes("1");
        return courseCategoryTreeDTOS;

    }
}
