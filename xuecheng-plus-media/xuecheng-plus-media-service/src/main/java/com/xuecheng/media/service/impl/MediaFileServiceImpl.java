package com.xuecheng.media.service.impl;

import com.alibaba.cloud.commons.io.IOUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
//@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    @Lazy
    private MediaFileService mediaFileService;
    @Autowired
    private MediaProcessMapper mediaProcessMapper;


    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;
    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

     @Override
     public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

      //构建查询条件对象
      LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.eq(MediaFiles::getCompanyId, companyId);
      queryWrapper.like(StringUtils.isNotBlank(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename());
      queryWrapper.eq(StringUtils.isNotBlank(queryMediaParamsDto.getFileType()),MediaFiles::getFileType, queryMediaParamsDto.getFileType());
      queryWrapper.eq(StringUtils.isNotBlank(queryMediaParamsDto.getAuditStatus()),MediaFiles::getAuditStatus, queryMediaParamsDto.getAuditStatus());

      //分页对象
      Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
      // 查询数据内容获得结果
      Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
      // 获取数据列表
      List<MediaFiles> list = pageResult.getRecords();
      // 获取数据总数
      long total = pageResult.getTotal();
      // 构建结果集
      PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
      return mediaListResult;

     }

    /**
     * 根据扩展名获取mimeType
     * @param extention
     * @return
     */
     private String getMimeType(String extention){
         if(extention == null){
             extention = " ";
         }
         //根据扩展名取出mimeType
         ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extention);
         String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
         if(extensionMatch != null) {
             mimeType = extensionMatch.getMimeType();
         }
         return mimeType;
     }


    /**
     * 将文件上传到minio
     * @param localFilePath 文件本地路径
     * @param mimeType 媒体类型
     * @param bucket 桶
     * @param objectName 对象名
     * @return
     */
    @Override
     public boolean addMediaFilesToMinio(String localFilePath, String mimeType, String bucket, String objectName){
         try {
             UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                     .bucket(bucket)
                     .filename(localFilePath)
                     .object(objectName)
                     .contentType(mimeType)
                     .build();
             //上传文件
             minioClient.uploadObject(uploadObjectArgs);
             log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             log.error("上传文件出错,bucket:{},objectName:{}", bucket, objectName);
         }
         return false;
     }

    /**
     * 获取文件默认存储目录 年/月/日
     * @return
     */
     private String getDefaultFolderPath(){
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
         String folder = simpleDateFormat.format(new Date()).replace("-", "/") + "/";
         return folder;
     }

    /**
     * 获取文件的md5
     * @param file
     * @return
     */
     public String getFileMd5(File file){
         try (FileInputStream fileInputStream = new FileInputStream(file)){
             String fileMd5 = DigestUtils.md5Hex(fileInputStream);
             return fileMd5;
         } catch (Exception e){
             e.printStackTrace();
             return null;
         }
     }

    /**
     * 上传文件
     * @param companyId  机构id
     * @param uploadFileParamDto 上传文件信息
     * @param localFilePath 文件磁盘路径
     * @return
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamDto uploadFileParamDto, String localFilePath, String objectName) {

        //1、将文件上传到minio
        //文件名
        String filename = uploadFileParamDto.getFilename();
        //文件扩展名
        String extention = filename.substring(filename.lastIndexOf("."));
        //根据扩展名取出mimeType
        String mimeType = getMimeType(extention);

        //获取文件路径，年/月/日
        String defaultFolderPath = getDefaultFolderPath();
        //文件的md5值
        String fileMd5 = getFileMd5(new File(localFilePath));
        if(objectName == null){
            objectName = defaultFolderPath + fileMd5 + extention;
        }
        //判断是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            //上传文件到minio
            boolean result = addMediaFilesToMinio(localFilePath, mimeType, bucket_mediafiles, objectName);
            if(!result){
                XueChengPlusException.cast("上传文件失败");
            }
            //2、将文件信息保存到数据库,获取代理对象，否则事务会失效
//            MediaFileServiceImpl currentProxy = (MediaFileServiceImpl) AopContext.currentProxy();
            mediaFiles = mediaFileService.addMediaFIlesToDb(companyId, uploadFileParamDto, fileMd5, bucket_mediafiles, objectName);
            if(mediaFiles == null){
                XueChengPlusException.cast("文件上传后保存信息失败");
            }
        }else {
            log.debug("该文件已经存在，fileMd5={}", fileMd5);
        }
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 将文件信息保存到数据库
     * @param companyId
     * @param uploadFileParamDto
     * @param fileMd5
     * @param bucket
     * @param objectName
     * @return
     */
    @Override
    @Transactional
    public MediaFiles addMediaFIlesToDb(Long companyId, UploadFileParamDto uploadFileParamDto, String fileMd5, String bucket, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamDto, mediaFiles);
            //文件id
            mediaFiles.setId(fileMd5);
            //机构id
            mediaFiles.setCompanyId(companyId);
            //桶
            mediaFiles.setBucket(bucket);
            //filePath
            mediaFiles.setFilePath(objectName);
            //file_id
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //状态
            mediaFiles.setStatus("1");
            //审核状态
            mediaFiles.setAuditStatus("002003");

            //插入数据
            int insert = mediaFilesMapper.insert(mediaFiles);
            if(insert <= 0){
                log.debug("向数据库保存文件失败,mediaFiles:{}",mediaFiles);
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息成功,mediaFiles:{}",mediaFiles);
            //记录待处理任务
            //向mediaProcerss插入记录
            addWaitingTask(mediaFiles);

        }
        return mediaFiles;

    }

    /**
     *添加待处理任务
     * @param mediaFiles
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        // 获取文件的mimetype
        String filename = mediaFiles.getFilename();
        String extention = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extention);

        //通过mimetype判断是否是avi视频
        if (mimeType.equals("video/x-msvideo")){
            //创建待处理任务
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setUrl(null);
            //写入待处理任务表
            mediaProcessMapper.insert(mediaProcess);
        }
    }


    /**
     * @description 检查文件是否存在
     * @param fileMd5 文件的md5
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @author Mr.M
     * @date 2022/9/13 15:38
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5){
        //1、查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles != null){
            //1.1 数据库中存在，查询minio

            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();

            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if(inputStream != null){
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //文件不存在
        return RestResponse.success(false);
    }

    /**
     * @description 检查分块是否存在
     * @param fileMd5  文件的md5
     * @param chunkIndex  分块序号
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean> false不存在，true存在
     * @author Mr.M
     * @date 2022/9/13 15:39
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex){

        //分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //分块文件地址
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFilePath)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if(inputStream != null){
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //分块不存在
        return RestResponse.success(false);
    }

    /**
     * 判断文件是否存在minio上
     * @param filePath
     * @param bucket
     * @return
     */
    public Boolean checkMinioVideo(String filePath, String bucket){
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(filePath)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if(inputStream != null){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

    /**
     * 上传分块到minio
     * @param fileMd5
     * @param chunkIndex
     * @param localChunkFilePath
     * @return
     */
    public RestResponse uploadChunk(String fileMd5, int chunkIndex, String localChunkFilePath){
        //获取mimeType
        String mimeType = getMimeType(null);
        //分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String objectName = chunkFileFolderPath + chunkIndex;

        //上传到minio
        boolean result = addMediaFilesToMinio(localChunkFilePath, mimeType, bucket_video, objectName);

        if(!result){
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        return RestResponse.success(true);
    }


    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    @Override
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream,1024);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 合并分块
     * @param companyId 机构id
     * @param fileMd5
     * @param chunkTotal 分块总数
     * @param uploadFileParamDto
     * @return
     */
    @Override
    public RestResponse mergeChunk(Long companyId, String fileMd5, int chunkTotal, UploadFileParamDto uploadFileParamDto){
        // 1、找到分块文件调用minio的SDK进行文件合并
        List<ComposeSource> sources = new ArrayList<>();
        //获取分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        for (int i = 0; i < chunkTotal; i++) {
            //指定分块文件信息
            ComposeSource composeSource = ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build();
            sources.add(composeSource);
        }
        // 合并
        //文件名
        String filename = uploadFileParamDto.getFilename();
        //文件扩展名
        String extName = filename.substring(filename.lastIndexOf("."));
        //合并文件路径
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);

        //指定合并后的文件信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(mergeFilePath)
                .sources(sources)
                .build();

        if(!checkMinioVideo(mergeFilePath, bucket_video)) {
            //minio合并文件
            try {
                minioClient.composeObject(composeObjectArgs);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("合并文件出错，bucket:{},objectName:{},错误信息:{}", bucket_video, mergeFilePath, e.getMessage());
                return RestResponse.validfail(false, "合并文件出错");
            }

            // 2、校验合并后的文件是否一致
            File file = downloadFileFromMinIO(bucket_video, mergeFilePath);
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                //计算md5
                String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
                if (!mergeFile_md5.equals(fileMd5)) {
                    log.error("检验合并文件md5值不一致，原始文件：{},合并文件：{}", fileMd5, mergeFile_md5);
                    return RestResponse.validfail(false, "文件校验失败");
                }
                //文件大小
                uploadFileParamDto.setFileSize(file.length());
            } catch (Exception e) {
                return RestResponse.validfail(false, "文件校验失败");
            }
        }
        // 3、将文件信息入库
//        MediaFileServiceImpl currentProxy = (MediaFileServiceImpl) AopContext.currentProxy();
        MediaFiles mediaFiles = mediaFileService.addMediaFIlesToDb(companyId, uploadFileParamDto, fileMd5, bucket_video, mergeFilePath);
        if(mediaFiles == null){
            return RestResponse.validfail(false, "文件入库失败");
        }

        // 4、清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 清理分块文件
     * @param chunkFolderPath 分块文件路径
     * @param chunkTotal 分块数量
     */
    private void clearChunkFiles(String chunkFolderPath, int chunkTotal){
        log.info("开始清理分块");
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFolderPath.concat(Integer.toString(i))))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket_video)
                .objects(objects)
                .build();

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        //要想真正删除

        try {
            for (Result<DeleteError> item : results) {
                DeleteError deleteError = item.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("分块清理失败");
        }
        log.info("分块清理完成！");

    }

    /**
     * 根据id获取媒资信息
     * @param id
     * @return
     */
    public MediaFiles getFileById(String id){
        return mediaFilesMapper.selectById(id);
    }

}
