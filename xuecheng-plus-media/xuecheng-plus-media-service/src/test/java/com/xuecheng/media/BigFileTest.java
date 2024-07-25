package com.xuecheng.media;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 测试分块上传
 */
@Slf4j
public class BigFileTest {

	//分块
	@Test
	public void testChunk() throws IOException {
		//源文件
		File sourceFile = new File("D:\\hnu\\video\\t1.mp4");
		//分块文件存储路径
		String chunkFilePath = "D:\\hnu\\video\\chunk\\";
		//分块文件大小
		int chunkSize = 1024 * 1024 * 5;
		//分块文件个数
		int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
		//从源文件中读数据，向分块文件中写数据
		RandomAccessFile r = new RandomAccessFile(sourceFile, "r");
		//缓冲区大小
		byte[] b = new byte[1024];
		for (int i = 0; i < chunkNum; i++) {
			//创建分块文件
			File file = new File(chunkFilePath + i);
			if(file.exists()){
				file.delete();
			}
			boolean newFile = file.createNewFile();
			if(newFile){
				//向分块文件中写入数据
				RandomAccessFile rw = new RandomAccessFile(file, "rw");
				int len = -1;
				while((len = r.read(b)) != -1){
					rw.write(b, 0, len);
					if(file.length() >= chunkSize){
						break;
					}
				}
				rw.close();
				System.out.println("完成分块" + i);
			}

		}
		r.close();

	}


	//合并
	@Test
	public void testMerge() throws IOException {
		//分块文件存储路径
		File chunkFolder = new File("D:\\hnu\\video\\chunk\\");
		//源文件
		File sourceFile = new File("D:\\hnu\\video\\t1.mp4");
		//合并后的文件
		File mergeFile = new File("D:\\hnu\\video\\t1_m.mp4");
		//取出所有分块文件
		File[] files = chunkFolder.listFiles();
		List<File> fileList = Arrays.asList(files);
		//排序
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
			}
		});
		//合并流
		RandomAccessFile rw = new RandomAccessFile(mergeFile, "rw");
		//缓冲区
		byte[] b = new byte[1024];
		for (File file : fileList) {
			RandomAccessFile r = new RandomAccessFile(file, "r");
			int len = -1;
			while ((len = r.read(b)) != -1){
				rw.write(b, 0, len);
			}
			r.close();
		}
		rw.close();
		//合并文件完成
		FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
		String md5_merge = DigestUtils.md5Hex(fileInputStream_merge);
		FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
		String md5_source = DigestUtils.md5Hex(fileInputStream_source);
		if(md5_merge.equals(md5_source)){
			System.out.println("合并成功");
		}

	}


}
