package com.check.love.domain.matching.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.check.love.domain.matching.entity.Match;
import com.check.love.domain.user.dto.UserDto.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MatchingDto {
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeRequest {
        private Long toUserId;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeResponse {
        private Long id;
        private UserResponse fromUser;
        private UserResponse toUser;
        private LocalDateTime createdAt;
        private boolean isMatch;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchResponse {
        private Long id;
        private UserResponse user;
        private LocalDateTime createdAt;
        private LocalDateTime lastMessageAt;
        private String lastMessageContent;
        private Long unreadMessageCount;
        
        public static MatchResponse from(Match match, UserResponse otherUser, 
                                        LocalDateTime lastMessageAt, String lastMessageContent, 
                                        Long unreadMessageCount) {
            return MatchResponse.builder()
                    .id(match.getId())
                    .user(otherUser)
                    .createdAt(match.getCreatedAt())
                    .lastMessageAt(lastMessageAt)
                    .lastMessageContent(lastMessageContent)
                    .unreadMessageCount(unreadMessageCount)
                    .build();
        }
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchListResponse {
        private List<MatchResponse> matches;
        private Long totalUnreadMessages;
    }
} 