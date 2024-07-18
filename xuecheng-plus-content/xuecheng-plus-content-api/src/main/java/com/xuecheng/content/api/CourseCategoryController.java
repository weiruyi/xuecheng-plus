package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CourseCategoryTreeDTO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "课程种类管理")
@RequiredArgsConstructor
@RestController
public class CourseCategoryController {

    @GetMapping("/course-category/tree-nodes")
    public CourseCategoryTreeDTO queryTreeNodes(){

        return null;
    }
}
