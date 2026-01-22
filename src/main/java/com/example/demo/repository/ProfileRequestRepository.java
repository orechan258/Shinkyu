package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ProfileRequest;

public interface ProfileRequestRepository extends JpaRepository<ProfileRequest, Long> {
    List<ProfileRequest> findByApply(Integer apply);
}