// DepartmentRepository.java
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    // 【追加】全ての部署情報をIDと名前で取得（部署は重複しないため、これでOK）
    // Department Entityがそのまま返される
    @Query("SELECT d FROM Department d ORDER BY d.id")
    List<Department> findAllDepartmentsOrderedById();
}