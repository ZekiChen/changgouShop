package com.changgou.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 文件上传Controller
 * @Author:      Chenzk
 * @CreateDate:  2020/1/3 0003 下午 8:59
 */
@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController {

    /**
     * 文件上传
     */
    @PostMapping
    public Result upload(@RequestParam(value = "file") MultipartFile file) throws Exception{
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),  // 文件全名：1.jpg
                file.getBytes(),  // 文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename())  // 文件扩展名
        );
        String[] uploads = FastDFSUtil.upload(fastDFSFile);// 将文件上传到FastDFS中

        // 拼接访问地址：url = http://192.168.211.132:8080/group1/M00/00/00/wKjThF0DBzaAP23MAAXz2mMp9oM26.jpeg
        String url = FastDFSUtil.getTrackerInfo() + "/" + uploads[0] + "/" + uploads[1];
        return new Result(true, StatusCode.OK, "上传成功！", url);
    }
}
