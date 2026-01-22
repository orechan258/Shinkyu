package com.example.demo.controller;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Request;
import com.example.demo.service.ApplicationService;

@Controller
@RequestMapping("/approve")
public class ApproverController {

    private final ApplicationService applicationService;

    public ApproverController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public String showApprovePage(Model model) {
        List<Request> allRequests = applicationService.findAll();
        LocalDateTime now = LocalDateTime.now();

        // 1. 承認待ち (Apply=0 かつ 期限内)
        List<Request> pendingRequests = allRequests.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && !isExpired(req, now))
                .collect(Collectors.toList());

        // 2. 確認済み (Apply=1 or 2)
        List<Request> approvedRequests = allRequests.stream()
                .filter(req -> req.getApply() != null && (req.getApply() == 1 || req.getApply() == 2))
                .collect(Collectors.toList());

        // 3. 期限切れ (Apply=0 かつ 期限切れ)
        List<Request> expiredRequests = allRequests.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && isExpired(req, now))
                .collect(Collectors.toList());

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("approvedRequests", approvedRequests);
        model.addAttribute("expiredRequests", expiredRequests);

        return "approve";
    }

    @PostMapping("/action")
    public String approveAction(@RequestParam Long requestId, @RequestParam String action) {
        boolean isApproved = "approve".equals(action);
        applicationService.updateApprovalStatus(requestId, isApproved);
        return "redirect:/approve";
    }

    // 期限切れ判定ヘルパー
    private boolean isExpired(Request req, LocalDateTime now) {
        if (req.getEndDate() == null || req.getEndTime() == null) {
            return false;
        }
        try {
            // Request EntityはLocalDate/LocalTimeを持っている場合とStringの場合があるが、
            // 既存コード(ApplicationController)を見るとDTOはString、EntityはLocalDate/Timeの可能性がある。
            // しかしRequest.javaを確認していないので、Entity定義に合わせる必要がある。
            // 前回のViewFileでRequest.javaはLocalDate/LocalTimeだった。
            // 比較するにはLocalDateTimeに変換する。

            LocalDateTime endDateTime = LocalDateTime.of(req.getEndDate(), req.getEndTime());
            return endDateTime.isBefore(now);
        } catch (Exception e) {
            return false;
        }
    }
}
