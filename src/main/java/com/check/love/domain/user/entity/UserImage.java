package com.check.love.domain.user.entity;

import com.check.love.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserImage extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String imageUrl;
    
    @Column(nullable = false)
    private String originalFileName;
    
    private String fileType;
    
    private Long fileSize;
    
    @Builder.Default
    private boolean isProfileImage = false;
    
    private Integer displayOrder;
} 