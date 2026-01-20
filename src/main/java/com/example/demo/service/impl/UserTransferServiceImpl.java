package com.example.demo.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.RequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserTransferServiceImpl {
	private final AppUserRepository appUserRepository;
	private final RequestRepository requestRepository;

	@Transactional
	public void transferUser(String userId, Long newGroupId) {

		// 1. ユーザの所属変更
		appUserRepository.updateGroup(userId, newGroupId);

		// 2. request を再同期
		requestRepository.resyncGroupIdByUser(userId, newGroupId);
	}
}
