package com.xuecheng.media;


import com.alibaba.cloud.commons.io.IOUtils;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.175.129:9000")
                    .credentials("minio", "...")
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

    //将分块文件上传到minio
    @Test
    public void uploadChunck() throws Exception {
        for (int i = 0; i < 3; i++) {
            //上传文件的参数信息
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .filename("D:\\hnu\\video\\chunk\\" +i)
                    .object("chunk/" + i)
                    .build();


            //上传文件
            minioClient.uploadObject(uploadObjectArgs);

            System.out.println("上传分块" + i + "成功");

        }
    }

    //调用minio接口合并分块
    @Test
    public void testMerge() throws Exception{
        List<ComposeSource> sources = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            //指定分块文件信息
            ComposeSource composeSource = ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build();
            sources.add(composeSource);
        }

        //指定合并后的文件信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mp4")
                .sources(sources)
                .build();

        //minio合并文件，minio默认的分块大小为5M
        minioClient.composeObject(composeObjectArgs);
    }

    //批量清理文件
    @Test
    public void clearChunkFiles(){
        String chunkFolderPath = "e/7/e714cf46e423840ed0d5cbc4d288a40b/chunk/";
        int chunkTotal = 4;
        log.info("开始清理分块");
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFolderPath.concat(Integer.toString(i))))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket("video")
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
            System.out.println("分块清理失败");
        }
        System.out.println("分块清理完成！");

    }



}
