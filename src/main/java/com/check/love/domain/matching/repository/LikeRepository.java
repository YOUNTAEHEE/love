package com.check.love.domain.matching.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.check.love.domain.matching.entity.Like;
import com.check.love.domain.user.entity.User;

public interface LikeRepository extends JpaRepository<Like, Long> {
    
    Optional<Like> findByFromUserAndToUser(User fromUser, User toUser);
    
    boolean existsByFromUserAndToUser(User fromUser, User toUser);
    
    List<Like> findByToUser(User toUser);
    
    @Query("SELECT l.toUser FROM Like l WHERE l.fromUser = :user")
    List<User> findLikedUsersByUser(@Param("user") User user);
    
    @Query("SELECT l.fromUser FROM Like l WHERE l.toUser = :user")
    List<User> findLikingUsersByUser(@Param("user") User user);
    
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Like l " +
           "WHERE (l.fromUser = :user1 AND l.toUser = :user2) OR (l.fromUser = :user2 AND l.toUser = :user1)")
    boolean existsMutualLike(@Param("user1") User user1, @Param("user2") User user2);
} 