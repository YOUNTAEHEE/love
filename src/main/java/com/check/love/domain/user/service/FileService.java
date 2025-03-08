package com.check.love.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.check.love.domain.user.entity.User;
import com.check.love.domain.user.entity.UserImage;
import com.check.love.domain.user.repository.UserImageRepository;
import com.check.love.domain.user.repository.UserRepository;
import com.check.love.global.exception.BusinessException;
import com.check.love.global.exception.ErrorCode;
import com.check.love.global.utils.FileUploadUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional
    public UserImage uploadUserImage(Long userId, MultipartFile file, boolean isProfileImage, Integer displayOrder) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 기존 프로필 이미지가 있고, 새 이미지가 프로필 이미지로 설정되려면 기존 이미지의 isProfileImage를 false로 변경
        if (isProfileImage) {
            userImageRepository.findByUserAndIsProfileImageTrue(user)
                    .ifPresent(profileImage -> {
                        profileImage.setProfileImage(false);
                        userImageRepository.save(profileImage);
                    });
        }

        // 파일 저장
        String fileName = FileUploadUtil.saveFile(uploadDir, file);
        String imageUrl = "/uploads/" + fileName;

        // UserImage 엔티티 생성 및 저장
        UserImage userImage = UserImage.builder()
                .user(user)
                .imageUrl(imageUrl)
                .originalFileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .isProfileImage(isProfileImage)
                .displayOrder(displayOrder)
                .build();

        return userImageRepository.save(userImage);
    }

    @Transactional
    public void deleteUserImage(Long userId, Long imageId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserImage userImage = userImageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "이미지를 찾을 수 없습니다."));

        // 이미지 소유자 확인
        if (!userImage.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "해당 이미지를 삭제할 권한이 없습니다.");
        }

        // 이미지 삭제
        userImageRepository.delete(userImage);
    }
} 