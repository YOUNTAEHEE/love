package com.check.love.global.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.check.love.global.exception.BusinessException;
import com.check.love.global.exception.ErrorCode;

public class FileUploadUtil {

    public static String saveFile(String uploadDir, MultipartFile multipartFile) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 파일 확장자 검증
            String originalFilename = multipartFile.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            
            if (!isValidImageExtension(fileExtension)) {
                throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
            }
            
            // 파일 크기 검증 (10MB 이하)
            if (multipartFile.getSize() > 10 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
            }
            
            // 저장할 파일명 생성 (UUID + 원본 확장자)
            String fileName = UUID.randomUUID() + "." + fileExtension;
            
            // 파일 저장
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                return fileName;
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
    
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private static boolean isValidImageExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif");
    }
} 