package com.example.demo.service.impl;

import org.springframework.stereotype.Service;

import com.example.demo.repository.RequestRepository;
import com.example.demo.service.RequestGroupSyncService;

@Service
public class RequestGroupSyncServiceImpl
		implements RequestGroupSyncService {

	private final RequestRepository requestRepository;

	public RequestGroupSyncServiceImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

}
