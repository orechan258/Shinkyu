package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
@Table(name = "profile_request")
public class ProfileRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private String userId;
    private String newLastName;
    private String newFirstName;
    private Integer newGroupId;
    private Integer apply; 
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- 【重要】ここから下を追加：DBには保存しない表示用フィールド ---
    @Transient
    private String currentLastName;
    @Transient
    private String currentFirstName;
    @Transient
    private String currentGroupName;
    @Transient
    private String newGroupName;
}