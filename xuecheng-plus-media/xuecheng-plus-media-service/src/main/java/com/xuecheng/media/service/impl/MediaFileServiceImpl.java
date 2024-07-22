package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements MediaFileService {

    private final MinioClient minioClient;
    private final MediaFilesMapper mediaFilesMapper;

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
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamDto uploadFileParamDto, String localFilePath) {

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
        String objectName = defaultFolderPath + fileMd5 + extention;
        //判断是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            //上传文件到minio
            boolean result = addMediaFilesToMinio(localFilePath, mimeType, bucket_mediafiles, objectName);
            if(!result){
                XueChengPlusException.cast("上传文件失败");
            }
            //2、将文件信息保存到数据库
            mediaFiles = addMediaFIlesToDb(companyId, uploadFileParamDto, fileMd5, bucket_mediafiles, objectName);
            if(mediaFiles == null){
                XueChengPlusException.cast("文件上传后保存信息失败");
            }
        }
        if(!mediaFiles.getCompanyId().equals(companyId)){
            log.info("其他公司已经上传过该资源");
            XueChengPlusException.cast("其他公司已经上传过该资源");
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
                log.debug("向数据库保存文件失败,bucket:{}, objectName:{}", bucket, objectName);
                return null;
            }
            return mediaFiles;
        }
        return mediaFiles;

    }


}
