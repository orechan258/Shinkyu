package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; // ★importを追加
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "request")
public class Request {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "request_id")
	private Long requestId; // 主キーとして使用

	// 既存の user_id は、主キーではなく通常の申請者IDに戻す
	@Column(name = "user_id", length = 10) // ★@Idアノテーションを削除
	private String userId; // 申請者IDとして使用

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "start_time")
	private LocalTime startTime;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "end_time")
	private LocalTime endTime;

	@Column(name = "Reason") // MySQLのCREATE TABLEに合わせて大文字Rを維持
	private String reason;

	@Column(name = "half_day")
	private Boolean halfDay;

	@Column(name = "sp_apply") // ★修正: Hibernateが探すスネークケースに明示的に設定
	private Boolean spApply;

	@Column(name = "Apply") // MySQLのCREATE TABLEに合わせて大文字Aを維持
	private Integer apply;

	@Column(name = "group_id")
	private Long groupId;

	public Request() {
	}

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	// 既存の getId() / setId() は getUserId() / setUserId() にリネームすることを推奨
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Boolean getHalfDay() {
		return halfDay;
	}

	public void setHalfDay(Boolean halfDay) {
		this.halfDay = halfDay;
	}

	public Boolean getSpApply() {
		return spApply;
	}

	public void setSpApply(Boolean spApply) {
		this.spApply = spApply;
	}

	public Integer getApply() {
		return apply;
	}

	public void setApply(Integer newStatus) {
		this.apply = newStatus;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
}