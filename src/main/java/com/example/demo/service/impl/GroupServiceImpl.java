package com.example.demo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Usergroup;
import com.example.demo.repository.GroupRepository;
import com.example.demo.service.GroupService;

@Service
public class GroupServiceImpl implements GroupService {

	private final GroupRepository groupRepository;

	public GroupServiceImpl(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	@Override
	public List<Usergroup> findAll() {
		return groupRepository.findAll();
	}

	@Override
	public List<Usergroup> findByDepartmentId(Integer departmentId) {
		return groupRepository.findByDepartmentId(departmentId);
	}
}
