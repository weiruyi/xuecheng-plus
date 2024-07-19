package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class CourseCategoryMapperTests {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testCourseCatergoryMapper() {

        List<CourseCategoryTreeDTO> courseCategoryTreeDTOS = courseCategoryMapper.selectTreeNodes("1");
        log.info(courseCategoryTreeDTOS.toString());

    }

}
