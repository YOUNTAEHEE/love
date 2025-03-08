package com.check.love.domain.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.check.love.domain.user.dto.UserDto.PasswordChangeRequest;
import com.check.love.domain.user.dto.UserDto.ProfileUpdateRequest;
import com.check.love.domain.user.dto.UserDto.SignUpRequest;
import com.check.love.domain.user.dto.UserDto.UserResponse;
import com.check.love.domain.user.entity.User;
import com.check.love.domain.user.entity.UserImage;
import com.check.love.domain.user.repository.UserImageRepository;
import com.check.love.domain.user.repository.UserRepository;
import com.check.love.global.exception.BusinessException;
import com.check.love.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UserResponse signUp(SignUpRequest request) {
        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        
        // 닉네임 중복 검사
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .bio(request.getBio())
                .location(request.getLocation())
                .height(request.getHeight())
                .job(request.getJob())
                .education(request.getEducation())
                .build();
        
        User savedUser = userRepository.save(user);
        
        return UserResponse.from(savedUser, null, new ArrayList<>());
    }
    
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        String profileImageUrl = null;
        List<String> imageUrls = new ArrayList<>();
        
        // 프로필 이미지 URL 가져오기
        Optional<UserImage> profileImage = userImageRepository.findByUserAndIsProfileImageTrue(user);
        if (profileImage.isPresent()) {
            profileImageUrl = profileImage.get().getImageUrl();
        }
        
        // 모든 이미지 URL 가져오기
        List<UserImage> userImages = userImageRepository.findByUserOrderByDisplayOrderAsc(user);
        if (!userImages.isEmpty()) {
            imageUrls = userImages.stream()
                    .map(UserImage::getImageUrl)
                    .collect(Collectors.toList());
        }
        
        return UserResponse.from(user, profileImageUrl, imageUrls);
    }
    
    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 닉네임 중복 검사 (변경된 경우에만)
        if (request.getNickname() != null && !request.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            user.setNickname(request.getNickname());
        }
        
        // 나머지 필드 업데이트
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        
        if (request.getJob() != null) {
            user.setJob(request.getJob());
        }
        
        if (request.getEducation() != null) {
            user.setEducation(request.getEducation());
        }
        
        User updatedUser = userRepository.save(user);
        
        String profileImageUrl = null;
        List<String> imageUrls = new ArrayList<>();
        
        // 프로필 이미지 URL 가져오기
        Optional<UserImage> profileImage = userImageRepository.findByUserAndIsProfileImageTrue(updatedUser);
        if (profileImage.isPresent()) {
            profileImageUrl = profileImage.get().getImageUrl();
        }
        
        // 모든 이미지 URL 가져오기
        List<UserImage> userImages = userImageRepository.findByUserOrderByDisplayOrderAsc(updatedUser);
        if (!userImages.isEmpty()) {
            imageUrls = userImages.stream()
                    .map(UserImage::getImageUrl)
                    .collect(Collectors.toList());
        }
        
        return UserResponse.from(updatedUser, profileImageUrl, imageUrls);
    }
    
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        
        // 새 비밀번호와 확인 비밀번호 일치 확인
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        
        // 비밀번호 암호화 및 업데이트
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        
        userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 사용자 이미지 삭제
        userImageRepository.deleteAllByUser(user);
        
        // 사용자 삭제
        userRepository.delete(user);
    }
} 