package com.check.love.domain.matching.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.check.love.domain.matching.dto.MatchingDto.LikeRequest;
import com.check.love.domain.matching.dto.MatchingDto.LikeResponse;
import com.check.love.domain.matching.dto.MatchingDto.MatchListResponse;
import com.check.love.domain.matching.dto.MatchingDto.MatchResponse;
import com.check.love.domain.matching.entity.Like;
import com.check.love.domain.matching.entity.Match;
import com.check.love.domain.matching.repository.LikeRepository;
import com.check.love.domain.matching.repository.MatchRepository;
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
public class MatchingService {
    
    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final LikeRepository likeRepository;
    private final MatchRepository matchRepository;
    private final MessageRepository messageRepository;
    
    /**
     * 좋아요 보내기
     */
    @Transactional
    public LikeResponse sendLike(Long fromUserId, LikeRequest request) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        // 자신에게 좋아요를 보낼 수 없음
        if (fromUser.getId().equals(toUser.getId())) {
            throw new BusinessException(ErrorCode.SELF_LIKE_NOT_ALLOWED);
        }
        
        // 이미 좋아요를 보냈는지 확인
        if (likeRepository.existsByFromUserAndToUser(fromUser, toUser)) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED);
        }
        
        // 좋아요 생성
        Like like = Like.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();
        
        Like savedLike = likeRepository.save(like);
        
        // 상대방도 나에게 좋아요를 보냈는지 확인 (매칭 여부)
        boolean isMatch = likeRepository.existsByFromUserAndToUser(toUser, fromUser);
        
        // 매칭이 성립되면 Match 엔티티 생성
        if (isMatch) {
            Match match = Match.builder()
                    .user1(fromUser)
                    .user2(toUser)
                    .build();
            
            matchRepository.save(match);
        }
        
        // 응답 생성
        UserResponse fromUserResponse = createUserResponse(fromUser);
        UserResponse toUserResponse = createUserResponse(toUser);
        
        return LikeResponse.builder()
                .id(savedLike.getId())
                .fromUser(fromUserResponse)
                .toUser(toUserResponse)
                .createdAt(savedLike.getCreatedAt())
                .isMatch(isMatch)
                .build();
    }
    
    /**
     * 좋아요 취소
     */
    @Transactional
    public void cancelLike(Long fromUserId, Long toUserId) {
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Like like = likeRepository.findByFromUserAndToUser(fromUser, toUser)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));
        
        likeRepository.delete(like);
        
        // 매치가 있으면 비활성화
        Optional<Match> matchOpt = matchRepository.findMatchBetweenUsers(fromUser, toUser);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            match.setActive(false);
            matchRepository.save(match);
        }
    }
    
    /**
     * 내가 좋아요 보낸 사용자 목록 조회
     */
    public List<UserResponse> getLikedUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        List<User> likedUsers = likeRepository.findLikedUsersByUser(user);
        
        return likedUsers.stream()
                .map(this::createUserResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 나에게 좋아요 보낸 사용자 목록 조회
     */
    public List<UserResponse> getLikingUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        List<User> likingUsers = likeRepository.findLikingUsersByUser(user);
        
        return likingUsers.stream()
                .map(this::createUserResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 매치 목록 조회
     */
    public MatchListResponse getMatches(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        List<Match> matches = matchRepository.findActiveMatchesByUser(user);
        
        List<MatchResponse> matchResponses = new ArrayList<>();
        
        for (Match match : matches) {
            // 상대방 사용자 정보 가져오기
            User otherUser = match.getUser1().getId().equals(userId) ? match.getUser2() : match.getUser1();
            UserResponse otherUserResponse = createUserResponse(otherUser);
            
            // 마지막 메시지 정보 가져오기
            LocalDateTime lastMessageAt = null;
            String lastMessageContent = null;
            
            // 읽지 않은 메시지 수 가져오기
            long unreadMessageCount = messageRepository.countUnreadMessagesByMatchAndUser(match, user);
            
            matchResponses.add(MatchResponse.from(match, otherUserResponse, lastMessageAt, lastMessageContent, unreadMessageCount));
        }
        
        // 전체 읽지 않은 메시지 수 가져오기
        long totalUnreadMessages = messageRepository.countUnreadMessages(user);
        
        return MatchListResponse.builder()
                .matches(matchResponses)
                .totalUnreadMessages(totalUnreadMessages)
                .build();
    }
    
    /**
     * 매치 상세 조회
     */
    public MatchResponse getMatch(Long userId, Long matchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
        
        // 매치 참여자인지 확인
        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_PARTICIPANT);
        }
        
        // 상대방 사용자 정보 가져오기
        User otherUser = match.getUser1().getId().equals(userId) ? match.getUser2() : match.getUser1();
        UserResponse otherUserResponse = createUserResponse(otherUser);
        
        // 마지막 메시지 정보 가져오기
        LocalDateTime lastMessageAt = null;
        String lastMessageContent = null;
        
        // 읽지 않은 메시지 수 가져오기
        long unreadMessageCount = messageRepository.countUnreadMessagesByMatchAndUser(match, user);
        
        return MatchResponse.from(match, otherUserResponse, lastMessageAt, lastMessageContent, unreadMessageCount);
    }
    
    /**
     * 사용자 응답 생성 헬퍼 메서드
     */
    private UserResponse createUserResponse(User user) {
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
} 