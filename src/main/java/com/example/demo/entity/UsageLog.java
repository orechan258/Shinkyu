package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usage_log")
@Getter
@Setter
public class UsageLog {

	@Id
	private Long id;

	// ユーザID
	private String userId;

	// ユーザ名
	private String userName;

	@ManyToOne
	@JoinColumn(name = "group_id")
	private Usergroup group;

	// 特認フラグ（0 or 1）
	private Integer specialApply;

	// 使用日時
	private LocalDateTime useDateTime;

	// 使用回数
	private Integer usageCount;

	// 申請理由
	private String reason;

	// 承認状態（例: "承認", "却下", "未承認"）
	private String approvalStatus;

	// ===== コントローラーで使用される Getter（明示的に追加） =====

	public boolean getSpecialApply() {
		// 特認フラグをbooleanで返す（CSVで使いやすいように）
		return specialApply != null && specialApply == 1;
	}

	public String getApproval() {
		return this.approvalStatus;
	}

	public Integer getUsageCount() {
		return usageCount;
	}
}
