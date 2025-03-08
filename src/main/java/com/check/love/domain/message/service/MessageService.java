package com.check.love.domain.message.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.check.love.domain.matching.entity.Match;
import com.check.love.domain.matching.repository.MatchRepository;
import com.check.love.domain.message.dto.MessageDto.MessageListResponse;
import com.check.love.domain.message.dto.MessageDto.MessageRequest;
import com.check.love.domain.message.dto.MessageDto.MessageResponse;
import com.check.love.domain.message.entity.Message;
import com.check.love.domain.message.repository.MessageRepository;
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
public class MessageService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;

    /**
     * 메시지 전송
     */
    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        // 매치 참여자인지 확인
        if (!isMatchParticipant(match, sender) || !isMatchParticipant(match, receiver)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_PARTICIPANT);
        }

        // 메시지 생성
        Message message = Message.builder()
                .match(match)
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .read(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        // 응답 생성
        UserResponse senderResponse = createUserResponse(sender);
        UserResponse receiverResponse = createUserResponse(receiver);

        return MessageResponse.from(savedMessage, senderResponse, receiverResponse);
    }

    /**
     * 메시지 목록 조회
     */
    public MessageListResponse getMessages(Long userId, Long matchId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        // 매치 참여자인지 확인
        if (!isMatchParticipant(match, user)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_PARTICIPANT);
        }

        // 메시지 조회
        Page<Message> messagePage = messageRepository.findByMatchOrderByCreatedAtDesc(match, pageable);

        // 응답 생성
        List<MessageResponse> messages = messagePage.getContent().stream()
                .map(message -> MessageResponse.from(
                        message,
                        createUserResponse(message.getSender()),
                        createUserResponse(message.getReceiver())
                ))
                .collect(Collectors.toList());

        return MessageListResponse.builder()
                .messages(messages)
                .totalPages(messagePage.getTotalPages())
                .totalElements(messagePage.getTotalElements())
                .currentPage(pageable.getPageNumber())
                .build();
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public void markMessagesAsRead(Long userId, Long matchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        // 매치 참여자인지 확인
        if (!isMatchParticipant(match, user)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_PARTICIPANT);
        }

        // 메시지 읽음 처리
        messageRepository.markMessagesAsRead(match, user);
    }

    /**
     * 읽지 않은 메시지 수 조회
     */
    public long countUnreadMessages(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return messageRepository.countUnreadMessages(user);
    }

    /**
     * 매치별 읽지 않은 메시지 수 조회
     */
    public long countUnreadMessagesByMatch(Long userId, Long matchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        // 매치 참여자인지 확인
        if (!isMatchParticipant(match, user)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_PARTICIPANT);
        }

        return messageRepository.countUnreadMessagesByMatchAndUser(match, user);
    }

    /**
     * 매치 참여자인지 확인하는 메서드
     */
    private boolean isMatchParticipant(Match match, User user) {
        return match.getUser1().getId().equals(user.getId()) || match.getUser2().getId().equals(user.getId());
    }

    /**
     * 사용자 응답 생성 헬퍼 메서드
     */
    private UserResponse createUserResponse(User user) {
        // final 변수 사용
        final String[] profileImageUrl = {null};
        List<String> imageUrls = userImageRepository.findByUserOrderByDisplayOrderAsc(user).stream()
                .map(UserImage::getImageUrl)
                .collect(Collectors.toList());

        userImageRepository.findByUserAndIsProfileImageTrue(user)
                .ifPresent(profileImage -> profileImageUrl[0] = profileImage.getImageUrl());

        return UserResponse.from(user, profileImageUrl[0], imageUrls);
    }
} 