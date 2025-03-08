package com.check.love.domain.message.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.check.love.domain.message.dto.MessageDto.MessageListResponse;
import com.check.love.domain.message.dto.MessageDto.MessageRequest;
import com.check.love.domain.message.dto.MessageDto.MessageResponse;
import com.check.love.domain.message.service.MessageService;
import com.check.love.domain.user.entity.User;
import com.check.love.domain.user.repository.UserRepository;
import com.check.love.global.exception.BusinessException;
import com.check.love.global.exception.ErrorCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MessageRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        MessageResponse response = messageService.sendMessage(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MessageListResponse> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long matchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        MessageListResponse response = messageService.getMessages(user.getId(), matchId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/matches/{matchId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long matchId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        messageService.markMessagesAsRead(user.getId(), matchId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnreadMessages(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        long count = messageService.countUnreadMessages(user.getId());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/matches/{matchId}/unread/count")
    public ResponseEntity<Long> countUnreadMessagesByMatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long matchId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        long count = messageService.countUnreadMessagesByMatch(user.getId(), matchId);
        return ResponseEntity.ok(count);
    }
} 