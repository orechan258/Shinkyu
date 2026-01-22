package com.example.demo.service;

public class RequestDetailDto {

    // Request Entityから取得するフィールド
    private Long requestId;       
    private String userId;        
    private String startDate;     
    private String startTime;     
    private String endDate;       
    private String endTime;       
    private String reason;        
    private boolean halfDay;      
    private boolean spApply;      
    private Integer apply;        
    
    // User Entityから結合して追加するフィールド
    private String applicantFullName; 

    // 【デフォルトコンストラクタ】
    public RequestDetailDto() {
    }
    
    // =========================================================
    // ゲッターとセッター (全てのフィールドについて定義)
    // =========================================================
    
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public boolean isHalfDay() { return halfDay; }
    public void setHalfDay(boolean halfDay) { this.halfDay = halfDay; }

    public boolean isSpApply() { return spApply; }
    public void setSpApply(boolean spApply) { this.spApply = spApply; }

    public Integer getApply() { return apply; }
    public void setApply(Integer apply) { this.apply = apply; }

    public String getApplicantFullName() { return applicantFullName; }
    public void setApplicantFullName(String applicantFullName) { this.applicantFullName = applicantFullName; }
}