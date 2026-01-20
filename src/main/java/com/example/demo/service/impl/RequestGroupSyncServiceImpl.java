package com.example.demo.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.RequestRepository;
import com.example.demo.service.RequestGroupSyncService;

@Service
public class RequestGroupSyncServiceImpl
		implements RequestGroupSyncService {

	private final RequestRepository requestRepository;

	public RequestGroupSyncServiceImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	@Transactional
	public int syncGroupId() {
		return requestRepository.syncGroupIdFromUser();
	}
}
