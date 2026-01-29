package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Usergroup;

public interface GroupRepository extends JpaRepository<Usergroup, Long> {

	List<Usergroup> findByDepartmentId(Integer departmentId);
}
