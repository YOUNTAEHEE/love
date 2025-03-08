package com.check.love.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.check.love.domain.user.entity.User;
import com.check.love.domain.user.entity.UserImage;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    
    List<UserImage> findByUserOrderByDisplayOrderAsc(User user);
    
    Optional<UserImage> findByUserAndIsProfileImageTrue(User user);
    
    void deleteAllByUser(User user);
} 