package com.xuecheng.media.api;


import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@Api(value = "大文件上传接口")
@RestController
@Slf4j
@RequiredArgsConstructor
public class BigFilesController {

	private final MediaFileService mediaFileService;

	@ApiOperation(value = "文件上传前检查文件")
	@PostMapping("/upload/checkfile")
	public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5){
		log.info("文件上传前检查文件，md5={}", fileMd5);
		RestResponse<Boolean> restResponse = mediaFileService.checkFile(fileMd5);
		return restResponse;
	}


	@ApiOperation(value = "分块文件上传前的检测")
	@PostMapping("/upload/checkchunk")
	public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk){
		log.info("分块上传前检查分块，md5={}", fileMd5);
		RestResponse<Boolean> restResponse = mediaFileService.checkChunk(fileMd5, chunk);
		return restResponse;
	}

	@ApiOperation(value = "上传分块文件")
	@PostMapping("/upload/uploadchunk")
	public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
	                                @RequestParam("fileMd5") String fileMd5,
	                                @RequestParam("chunk") int chunk) throws IOException {
		log.info("上传分块文件，fileMd5={}, chunk={}", fileMd5, chunk);
		//创建临时文件
		File tempFile = File.createTempFile("minio", ",temp");
		file.transferTo(tempFile);
		RestResponse restResponse = mediaFileService.uploadChunk(fileMd5, chunk, tempFile.getAbsolutePath());
		return restResponse;
	}

	@ApiOperation(value = "合并文件")
	@PostMapping("/upload/mergechunks")
	public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
	                                @RequestParam("fileName") String fileName,
	                                @RequestParam("chunkTotal") int chunkTotal){

		log.info("合并文件，fileMd5={}, fileName={}", fileMd5, fileName);
		// TODO:companyId
		Long companyId = 1232141425L;

		//文件信息对象
		UploadFileParamDto uploadFileParamDto = new UploadFileParamDto();
		uploadFileParamDto.setFilename(fileName);
		uploadFileParamDto.setFileType("001002");
		uploadFileParamDto.setTags("视频文件");

		RestResponse restResponse = mediaFileService.mergeChunk(companyId, fileMd5, chunkTotal, uploadFileParamDto);
		return restResponse;
	}

}
