package com.check.love.domain.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.check.love.domain.common.BaseTimeEntity;
import com.check.love.domain.user.enums.Gender;
import com.check.love.domain.user.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String nickname;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @Column(nullable = false)
    private LocalDate birthDate;
    
    private String bio;
    
    private String location;
    
    private Integer height;
    
    private String job;
    
    private String education;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;
    
    @Builder.Default
    private boolean enabled = true;
    
    @Builder.Default
    private LocalDateTime lastLoginAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private Set<UserImage> images = new HashSet<>();
    
    public void addImage(UserImage image) {
        this.images.add(image);
        image.setUser(this);
    }
    
    public void removeImage(UserImage image) {
        this.images.remove(image);
        image.setUser(null);
    }
} 