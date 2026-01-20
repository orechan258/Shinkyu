package com.example.demo.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Request;
import com.example.demo.repository.RequestRepository;
import com.example.demo.service.UsageLogCsvService;

@Service
public class UsageLogCsvServiceImpl implements UsageLogCsvService {

	private final RequestRepository requestRepository;

	public UsageLogCsvServiceImpl(RequestRepository requestRepository) {
		this.requestRepository = requestRepository;
	}

	@Override
	public ResponseEntity<byte[]> exportByGroups(List<Long> groupIds) {

		List<Request> requests;

		if (groupIds == null || groupIds.isEmpty()) {
			// 全社 ＋ 承認済みのみ
			requests = requestRepository.findByApply(1);
		} else {
			// 指定グループ ＋ 承認済みのみ
			requests = requestRepository.findByGroupIdInAndApply(groupIds, 1);
		}

		// ↓ ここからCSV生成
		StringBuilder csv = new StringBuilder();
		csv.append("申請ID,ユーザーID,開始日,終了日,理由\n");

		for (Request r : requests) {
			csv.append(r.getRequestId()).append(",");
			csv.append(r.getUserId()).append(",");
			csv.append(r.getStartDate()).append(",");
			csv.append(r.getEndDate()).append(",");
			csv.append(r.getReason()).append("\n");
		}

		byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=request.csv");

		return ResponseEntity.ok()
				.headers(headers)
				.body(bytes);
	}

}
