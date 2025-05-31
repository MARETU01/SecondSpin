package com.secondspin.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImagesUtils {

    public static List<String> saveProductImages(MultipartFile[] files) throws IOException {
        List<String> imageNames = new ArrayList<>();
        if (files == null || files.length == 0) {
            return imageNames;
        }

         String savePath = new File(System.getProperty("user.dir") + "/../secondspin-app/public/images/products").getCanonicalPath();

        // 确保保存路径存在
        File directory = new File(savePath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("无法创建保存图片的目录: " + savePath);
            }
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {

                 String uniqueFileName = generateUniqueFilename(file);

                // 保存文件
                File destinationFile = new File(directory, uniqueFileName);
                file.transferTo(destinationFile);

                // 添加文件名到列表
                imageNames.add(uniqueFileName);
            }
        }

        return imageNames;
    }

    public static String saveAvatar(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String savePath = new File(System.getProperty("user.dir") + "/../secondspin-app/public/images/avatar").getCanonicalPath();

        File directory = new File(savePath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("无法创建保存头像的目录: " + savePath);
            }
        }

        String uniqueFileName = generateUniqueFilename(file);

        // 保存文件
        File destinationFile = new File(directory, uniqueFileName);
        file.transferTo(destinationFile);

        return uniqueFileName;
    }

    private static String generateUniqueFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}