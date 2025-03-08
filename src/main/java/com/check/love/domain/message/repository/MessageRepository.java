package com.check.love.domain.message.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.check.love.domain.matching.entity.Match;
import com.check.love.domain.message.entity.Message;
import com.check.love.domain.user.entity.User;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    Page<Message> findByMatchOrderByCreatedAtDesc(Match match, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.match = :match ORDER BY m.createdAt DESC")
    List<Message> findLatestMessagesByMatch(@Param("match") Match match, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :user AND m.read = false")
    long countUnreadMessages(@Param("user") User user);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.match = :match AND m.receiver = :user AND m.read = false")
    long countUnreadMessagesByMatchAndUser(@Param("match") Match match, @Param("user") User user);
    
    @Modifying
    @Query("UPDATE Message m SET m.read = true WHERE m.match = :match AND m.receiver = :user AND m.read = false")
    void markMessagesAsRead(@Param("match") Match match, @Param("user") User user);
} 