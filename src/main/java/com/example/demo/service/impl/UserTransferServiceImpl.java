package com.example.demo.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.UserTransferService;

@Service
@Transactional(readOnly = true)
public class UserTransferServiceImpl implements UserTransferService {

	private final AppUserRepository appUserRepository;

	public UserTransferServiceImpl(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	/**
	 * 承認済みユーザーの userId → groupId(Long) の Map を返す
	 */
	@Override
	public Map<String, Long> getApprovedUserGroupMap(List<Integer> groupIds) {

		// 承認済み(apply = 1) かつ 指定グループのユーザーを取得
		List<User> users = appUserRepository.findByGroupIdIn(groupIds);

		// userId -> groupId(Long) の Map に変換
		return users.stream()
				.collect(Collectors.toMap(
						User::getUserId,
						User::getGroupId));
	}
}
