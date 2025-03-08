package com.check.love.domain.matching.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.check.love.domain.matching.dto.MatchingDto.LikeRequest;
import com.check.love.domain.matching.dto.MatchingDto.LikeResponse;
import com.check.love.domain.matching.dto.MatchingDto.MatchListResponse;
import com.check.love.domain.matching.dto.MatchingDto.MatchResponse;
import com.check.love.domain.matching.service.MatchingService;
import com.check.love.domain.user.dto.UserDto.UserResponse;
import com.check.love.domain.user.entity.User;
import com.check.love.domain.user.repository.UserRepository;
import com.check.love.global.exception.BusinessException;
import com.check.love.global.exception.ErrorCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;
    private final UserRepository userRepository;

    @PostMapping("/likes")
    public ResponseEntity<LikeResponse> sendLike(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LikeRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        LikeResponse response = matchingService.sendLike(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/likes/{toUserId}")
    public ResponseEntity<Void> cancelLike(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long toUserId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        matchingService.cancelLike(user.getId(), toUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/liked-users")
    public ResponseEntity<List<UserResponse>> getLikedUsers(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<UserResponse> response = matchingService.getLikedUsers(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/liking-users")
    public ResponseEntity<List<UserResponse>> getLikingUsers(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<UserResponse> response = matchingService.getLikingUsers(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/matches")
    public ResponseEntity<MatchListResponse> getMatches(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        MatchListResponse response = matchingService.getMatches(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MatchResponse> getMatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long matchId) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        MatchResponse response = matchingService.getMatch(user.getId(), matchId);
        return ResponseEntity.ok(response);
    }
} 