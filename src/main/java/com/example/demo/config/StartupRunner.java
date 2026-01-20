package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.service.RequestGroupSyncService;

@Component
public class StartupRunner implements CommandLineRunner {

	private final RequestGroupSyncService syncService;

	public StartupRunner(RequestGroupSyncService syncService) {
		this.syncService = syncService;
	}

	@Override
	public void run(String... args) {
		int count = syncService.syncGroupId();
		System.out.println("request.group_id 同期件数: " + count);
	}
}
