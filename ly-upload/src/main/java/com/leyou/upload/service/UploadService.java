package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UploadService {
    private static List<String> types = Arrays.asList("image/png", "image/jpeg", "image/bmp", "image/jpg", "image/gif");

    public String upload(MultipartFile file) {
//    1.判断上传的是否为图片， png bpm jpg jpeg gif
        String contentType = file.getContentType();
        if (!types.contains(contentType)) {
    throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
//    2.校验图片内容
        BufferedImage image=null;
        try {
            image= ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        if (image==null){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
//     3.保存图片，生成保存目录，保存到nginx所在目录html下，目的可以通过nginx直接访问
        String filename = UUID.randomUUID() + file.getOriginalFilename();
        File dir = new File("D:\\develop\\nginx-1.14.0\\html\\");
        //校验目录是否存在，如不存在，就创建
        if (!dir.exists()){
            dir.mkdirs();
        }
        try {
            //保存图片
            file.transferTo(new File(dir,filename));
            //拼接图片地址
            return "http://image.leyou.com/" + filename;
        } catch (IOException e) {
           throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }

    @Autowired
    private OSSProperties prop;

    @Autowired
    private OSS client;

    public Map<String, Object> getSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }
}
