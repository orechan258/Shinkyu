package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Usergroup;

public interface GroupService {

	List<Usergroup> findAll();

	List<Usergroup> findByDepartmentId(Integer departmentId);
}
