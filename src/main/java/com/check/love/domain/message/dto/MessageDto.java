package com.check.love.domain.message.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.check.love.domain.message.entity.Message;
import com.check.love.domain.user.dto.UserDto.UserResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MessageDto {
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageRequest {
        private Long matchId;
        private Long receiverId;
        private String content;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private Long id;
        private Long matchId;
        private UserResponse sender;
        private UserResponse receiver;
        private String content;
        private boolean read;
        private LocalDateTime createdAt;
        
        public static MessageResponse from(Message message, UserResponse sender, UserResponse receiver) {
            return MessageResponse.builder()
                    .id(message.getId())
                    .matchId(message.getMatch().getId())
                    .sender(sender)
                    .receiver(receiver)
                    .content(message.getContent())
                    .read(message.isRead())
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageListResponse {
        private List<MessageResponse> messages;
        private int totalPages;
        private long totalElements;
        private int currentPage;
    }
} 