package com.leyou.upload.controller;

import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class UploadController {

    /**
     * 上传图片功能
     * @param file
     * @return url路径
     */
    @Autowired
    private UploadService uploadService;

    @PostMapping(value = "/image", name = "本地图片上传")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
//          返回200，并且携带url路径
        return ResponseEntity.ok(this.uploadService.upload(file));
    }
    @GetMapping(value = "/signature", name = "获取阿里云签名")
    public ResponseEntity<Map> signature() {
//          返回200，并且携带url路径
        return ResponseEntity.ok(uploadService.getSignature());
    }
}
