package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.UserGroup; // 新しくUserGroup Entityを作成する必要があります

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Integer> {
    
}