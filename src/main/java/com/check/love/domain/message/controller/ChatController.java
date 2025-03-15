package com.check.love.domain.message.controller;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.check.love.domain.message.dto.ChatMessage;
import com.check.love.domain.message.dto.MessageDto;
import com.check.love.domain.message.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        log.info("Received message: {}", chatMessage);
        
        // 메시지에 현재 시간 설정
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }

        // 채팅 메시지 저장
        MessageDto.MessageRequest messageRequest = MessageDto.MessageRequest.builder()
            .matchId(chatMessage.getMatchId())
            .receiverId(chatMessage.getReceiverId())
            .content(chatMessage.getContent())
            .build();
        
        messageService.sendMessage(chatMessage.getSenderId(), messageRequest);

        // 상대방에게 메시지 전송
        messagingTemplate.convertAndSendToUser(
            chatMessage.getReceiverId().toString(),
            "/queue/messages",
            chatMessage
        );
    }
} 