package com.check.love.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.check.love.api.AuthApi;
import com.check.love.domain.user.entity.User;
import com.check.love.domain.user.repository.UserRepository;
import com.check.love.global.exception.BusinessException;
import com.check.love.global.exception.ErrorCode;
import com.check.love.global.security.jwt.JwtTokenProvider;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<TokenResponse> authLogin(@Valid @RequestBody LoginRequest loginRequest) {
        // 사용자의 loginRequest에서 인증 정보 추출
        String email = loginRequest.getAccountInfo().getUsername();
        String password = loginRequest.getAccountInfo().getPassword();
        
        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        // SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 토큰 생성
        String jwt = jwtTokenProvider.createToken(authentication);

        // 사용자 ID 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 마지막 로그인 시간 업데이트
        userRepository.save(user);

        // OpenAPI 모델 사용하여 응답 생성
        TokenResponse tokenResponse = new TokenResponse();
        TokenResponse.AuthToken authToken = new TokenResponse.AuthToken();
        authToken.setToken(jwt);
        authToken.setTokenType("Bearer");
        authToken.setTokenExpiresAt(null); // 필요시 만료 시간 설정
        tokenResponse.setAuthToken(authToken);

        return ResponseEntity.ok(tokenResponse);
    }
    
    // 다른 API 메서드 구현 추가...
} 