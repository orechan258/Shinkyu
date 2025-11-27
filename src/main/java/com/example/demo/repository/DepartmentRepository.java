package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Department; // 新しくDepartment Entityを作成する必要があります

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    
}