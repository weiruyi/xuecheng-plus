package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.TeachplanDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程计划管理
 */
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@Slf4j
@RestController
@RequestMapping("/teachplan")
public class TeachplanController {

    //查询课程计划
    @ApiOperation("查询课程计划树形结构")
    @GetMapping("{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable("courseId") String courseId) {

        return null;
    }

}
