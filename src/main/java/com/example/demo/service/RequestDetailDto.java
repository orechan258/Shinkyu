package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.demo.entity.Request;

// 申請情報と申請者名を結合してControllerに渡すためのDTO
public class RequestDetailDto {
    private final Request request;
    private final String applicantFullName;

    // コンストラクタ
    public RequestDetailDto(Request request, String applicantFullName) {
        this.request = request;
        this.applicantFullName = applicantFullName;
    }

    // --- Request Entityのフィールドへの委譲 ---
    // (Thymeleafで ${req.startDate} のようにアクセスできるように、主要フィールドのGetterを定義)

    public Long getRequestId() { return request.getRequestId(); }
    public String getUserId() { return request.getUserId(); }
    public LocalDate getStartDate() { return request.getStartDate(); }
    public LocalTime getStartTime() { return request.getStartTime(); }
    public LocalDate getEndDate() { return request.getEndDate(); }
    public LocalTime getEndTime() { return request.getEndTime(); }
    public String getReason() { return request.getReason(); }
    public Boolean getHalfDay() { return request.getHalfDay(); }
    public Boolean getSpApply() { return request.getSpApply(); }
    public Integer getApply() { return request.getApply(); }

    // --- 新しいフィールド ---
    public String getApplicantFullName() {
        return applicantFullName;
    }
}