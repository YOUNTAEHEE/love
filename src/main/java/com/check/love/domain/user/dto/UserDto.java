package com.check.love.domain.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.check.love.domain.user.entity.User;
import com.check.love.domain.user.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserDto {
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpRequest {
        private String email;
        private String password;
        private String nickname;
        private Gender gender;
        private LocalDate birthDate;
        private String bio;
        private String location;
        private Integer height;
        private String job;
        private String education;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String email;
        private String nickname;
        private Gender gender;
        private LocalDate birthDate;
        private String bio;
        private String location;
        private Integer height;
        private String job;
        private String education;
        private String profileImageUrl;
        private List<String> imageUrls;
        private LocalDateTime createdAt;
        
        public static UserResponse from(User user, String profileImageUrl, List<String> imageUrls) {
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .gender(user.getGender())
                    .birthDate(user.getBirthDate())
                    .bio(user.getBio())
                    .location(user.getLocation())
                    .height(user.getHeight())
                    .job(user.getJob())
                    .education(user.getEducation())
                    .profileImageUrl(profileImageUrl)
                    .imageUrls(imageUrls)
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileUpdateRequest {
        private String nickname;
        private String bio;
        private String location;
        private Integer height;
        private String job;
        private String education;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordChangeRequest {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;
    }
} 