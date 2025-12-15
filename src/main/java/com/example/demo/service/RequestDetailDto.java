package com.example.demo.service;

// DTOのため、Entityのimportは不要

public class RequestDetailDto {

    // Request Entityから取得するフィールド
    private Long requestId;       // 申請ID (主キー)
    private String userId;        // 申請者ユーザーID
    private String startDate;     // 開始日
    private String startTime;     // 開始時刻
    private String endDate;       // 終了日
    private String endTime;       // 終了時刻
    private String reason;        // 理由
    private boolean halfDay;      // 半日申請フラグ
    private boolean spApply;      // 特認申請フラグ
    private Integer apply;        // 承認ステータス (0:未承認, 1:承認, 2:拒否, 3:キャンセルなど)
    
    // User Entityから結合して追加するフィールド
    private String applicantFullName; // 申請者氏名 (姓 + 名)

    // =========================================================
    // 【エラー解消済み】デフォルトコンストラクタ (ApplicationServiceImplで使用)
    // =========================================================
    public RequestDetailDto() {
    }

    // =========================================================
    // ゲッターとセッター
    // =========================================================

    // requestId
    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    // userId
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // startDate
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    // startTime
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    // endDate
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    // endTime
    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    // reason
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // halfDay
    public boolean isHalfDay() {
        return halfDay;
    }

    public void setHalfDay(boolean halfDay) {
        this.halfDay = halfDay;
    }

    // spApply
    public boolean isSpApply() {
        return spApply;
    }

    public void setSpApply(boolean spApply) {
        this.spApply = spApply;
    }

    // apply
    public Integer getApply() {
        return apply;
    }

    public void setApply(Integer apply) {
        this.apply = apply;
    }

    // applicantFullName
    public String getApplicantFullName() {
        return applicantFullName;
    }

    public void setApplicantFullName(String applicantFullName) {
        this.applicantFullName = applicantFullName;
    }
}