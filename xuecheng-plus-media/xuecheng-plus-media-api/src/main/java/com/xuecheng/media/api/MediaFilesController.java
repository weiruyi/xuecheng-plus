package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2022/9/6 11:29
 */
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {

	@Autowired
	MediaFileService mediaFileService;


	@ApiOperation("媒资列表查询接口")
	@PostMapping("/files")
	public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
		Long companyId = 1232141425L;
		return mediaFileService.queryMediaFiels(companyId, pageParams, queryMediaParamsDto);

	}

	@ApiOperation("上传图片")
	@PostMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile filedata) throws IOException {

		//1、准备上传文件的信息
		UploadFileParamDto uploadFileParamDto = new UploadFileParamDto();
        //原始文件名
        uploadFileParamDto.setFilename(filedata.getOriginalFilename());
        //文件大小
        uploadFileParamDto.setFileSize(filedata.getSize());
        //文件类型
        uploadFileParamDto.setFileType("001001");

		//2、创建临时文件
		File tempFile = File.createTempFile("minio", ".temp");
		filedata.transferTo(tempFile);

        //3、文件路径
        String localFilePath = tempFile.getAbsolutePath();

		//4、TODO:companyId
		Long companyId = 1232141425L;

		//调用sevice上传图片
        UploadFileResultDto uploadFileResultDto = mediaFileService.uploadFile(companyId, uploadFileParamDto, localFilePath);
        return uploadFileResultDto;
	}

}
