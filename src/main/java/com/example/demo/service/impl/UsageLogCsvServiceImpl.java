package com.example.demo.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.repository.RequestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UsageLogCsvService;

@Service
public class UsageLogCsvServiceImpl implements UsageLogCsvService {

	private final RequestRepository requestRepository;
	private final UserRepository userRepository;

	public UsageLogCsvServiceImpl(RequestRepository requestRepository,
			UserRepository userRepository) {
		this.requestRepository = requestRepository;
		this.userRepository = userRepository;
	}

	@Override
	public ResponseEntity<byte[]> exportByGroups(List<Integer> groupIds) {

		/* ========= ① 承認済み申請のみ取得 ========= */
		List<Request> requests = requestRepository.findApproved(); // apply = 1

		if (requests.isEmpty()) {
			return createEmptyCsv();
		}

		/* ========= ② userId 一覧抽出 ========= */
		List<String> userIds = requests.stream()
				.map(Request::getUserId)
				.distinct()
				.toList();

		/* ========= ③ User取得 ========= */
		List<User> users = userRepository.findByUserIdIn(userIds);

		Map<String, Integer> userGroupMap = users.stream()
				.collect(Collectors.toMap(
						User::getUserId,
						User::getGroupId));

		/* ========= ④ groupIds 指定がある場合は絞り込み ========= */
		if (groupIds != null && !groupIds.isEmpty()) {
			requests = requests.stream()
					.filter(r -> groupIds.contains(userGroupMap.get(r.getUserId())))
					.toList();
		}

		/* ========= ⑤ CSV生成 ========= */
		String csv = buildCsv(requests, userGroupMap);

		/* ========= ⑥ レスポンス ========= */
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usage_log.csv");
		headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

		return new ResponseEntity<>(
				csv.getBytes(StandardCharsets.UTF_8),
				headers,
				HttpStatus.OK);
	}

	/* ================= CSV生成 ================= */

	private String buildCsv(List<Request> requests, Map<String, Integer> userGroupMap) {

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

		StringBuilder sb = new StringBuilder();

		// ヘッダ
		sb.append("ユーザーID,グループID,開始日,開始時刻,終了日,終了時刻,理由\n");

		for (Request r : requests) {
			sb.append(r.getUserId()).append(",");
			sb.append(userGroupMap.get(r.getUserId())).append(",");
			sb.append(r.getStartDate().format(dateFormatter)).append(",");
			sb.append(r.getStartTime().format(timeFormatter)).append(",");
			sb.append(r.getEndDate().format(dateFormatter)).append(",");
			sb.append(r.getEndTime().format(timeFormatter)).append(",");
			sb.append(escapeCsv(r.getReason())).append("\n");
		}

		return sb.toString();
	}

	private String escapeCsv(String value) {
		if (value == null) {
			return "";
		}
		return "\"" + value.replace("\"", "\"\"") + "\"";
	}

	private ResponseEntity<byte[]> createEmptyCsv() {
		String headerOnly = "ユーザーID,グループID,開始日,開始時刻,終了日,終了時刻,理由\n";
		return ResponseEntity.ok(headerOnly.getBytes(StandardCharsets.UTF_8));
	}
}
