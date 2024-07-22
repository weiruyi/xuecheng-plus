package com.xuecheng.media;


import com.alibaba.cloud.commons.io.IOUtils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://49.234.52.192:9000")
                    .credentials("minio", "wry@0312.")
                    .build();

    @Test
    public void testUpload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        //通过扩展名获取媒体类型
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        //上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")
                .filename("D:\\hnu\\photo\\122.jpg")
                .object("test/1.jpg")
                .contentType(mimeType)
                .build();


        //上传文件
        minioClient.uploadObject(uploadObjectArgs);


    }

    @Test
    public void testDelete() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("1.jpg").build();

        //上传文件
        minioClient.removeObject(removeObjectArgs);


    }


    @Test
    public void test_getFile() throws Exception{

        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("test/1.jpg").build();

        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("D:\\hnu\\photo\\222.jpg"));
        IOUtils.copy(inputStream, fileOutputStream, 1024);

        //校验文件完整性
        String sourse_md5 = DigestUtils.md5Hex(inputStream);
        FileInputStream fileInputStream = new FileInputStream(new File("D:\\hnu\\photo\\222.jpg"));
        String local_md5 = DigestUtils.md5Hex(fileInputStream);
        if(local_md5.equals(sourse_md5)) {
            System.out.println("下载成功");
        }else {
            System.out.println("下载失败");
        }
    }



}
