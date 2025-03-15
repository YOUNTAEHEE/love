package com.check.love.domain.message.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    private MessageType type;
    private Long matchId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private LocalDateTime timestamp;
    
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
} 