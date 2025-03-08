package com.check.love.domain.matching.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.check.love.domain.matching.entity.Match;
import com.check.love.domain.user.entity.User;

public interface MatchRepository extends JpaRepository<Match, Long> {
    
    @Query("SELECT m FROM Match m WHERE (m.user1 = :user OR m.user2 = :user) AND m.active = true")
    List<Match> findActiveMatchesByUser(@Param("user") User user);
    
    @Query("SELECT m FROM Match m WHERE (m.user1 = :user1 AND m.user2 = :user2) OR (m.user1 = :user2 AND m.user2 = :user1)")
    Optional<Match> findMatchBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Match m " +
           "WHERE ((m.user1 = :user1 AND m.user2 = :user2) OR (m.user1 = :user2 AND m.user2 = :user1)) AND m.active = true")
    boolean existsActiveMatchBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);
} 