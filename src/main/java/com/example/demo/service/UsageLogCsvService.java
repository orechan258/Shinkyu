package com.example.demo.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface UsageLogCsvService {

	/**
	 * 在宅申請（Request）利用状況CSVを生成する
	 *
	 * @param startDate 開始日
	 * @param endDate   終了日
	 * @param scope     出力範囲（company / user / group）
	 * @param userId    ユーザID（scope = user の場合のみ使用）
	 * @param groupId   グループID（scope = group の場合のみ使用）
	 * @return CSV文字列
	 */

	public ResponseEntity<byte[]> exportByGroups(List<Integer> groupIds);
}
