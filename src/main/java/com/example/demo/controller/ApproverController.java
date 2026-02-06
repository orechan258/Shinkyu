package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.RequestDetailDto;

@Controller
@RequestMapping("/approve")
public class ApproverController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public ApproverController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showApprovePage(Model model) {
        List<RequestDetailDto> allRequests = applicationService.findAllWithNames();
        LocalDateTime now = LocalDateTime.now();

        // 1. 承認待ち (Apply=0 かつ 期限内)
        List<RequestDetailDto> pendingRequests = allRequests.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && !isExpiredDto(req, now))
                .collect(Collectors.toList());

        // 2. 確認済み (Apply=1 or 2)
        List<RequestDetailDto> approvedRequests = allRequests.stream()
                .filter(req -> req.getApply() != null && (req.getApply() == 1 || req.getApply() == 2))
                .collect(Collectors.toList());

        // 3. 期限切れ (Apply=0 かつ 期限切れ)
        List<RequestDetailDto> expiredRequests = allRequests.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && isExpiredDto(req, now))
                .collect(Collectors.toList());

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("approvedRequests", approvedRequests);
        model.addAttribute("expiredRequests", expiredRequests);

        return "approve";
    }

    @PostMapping("/action")
    public String approveAction(@RequestParam Long requestId, @RequestParam String action,
            Authentication authentication, RedirectAttributes ra) {

        // 現在のユーザーIDを取得
        String currentUserId = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                String email = oAuth2User.getAttribute("email");
                currentUserId = userRepository.findByEmail(email)
                        .map(com.example.demo.entity.User::getUserId)
                        .orElse(null);
            } else {
                currentUserId = authentication.getName();
            }
        }

        if (currentUserId == null) {
            ra.addFlashAttribute("error", "ユーザー情報を取得できませんでした。");
            return "redirect:/approve";
        }

        try {
            boolean isApproved = "approve".equals(action);
            applicationService.updateApprovalStatus(requestId, isApproved, currentUserId);
            ra.addFlashAttribute("message", isApproved ? "承認しました。" : "却下しました。");
        } catch (SecurityException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "処理中にエラーが発生しました: " + e.getMessage());
        }

        return "redirect:/approve";
    }

    // 期限切れ判定ヘルパー (DTO用)
    private boolean isExpiredDto(RequestDetailDto req, LocalDateTime now) {
        if (req.getEndDate() == null || req.getEndTime() == null) {
            return false;
        }
        try {
            // DTOはStringで時刻を持つため、LocalDateTime.parse を使用する
            String endDateTimeStr = req.getEndDate() + " " + req.getEndTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr, formatter);
            return endDateTime.isBefore(now);
        } catch (Exception e) {
            return false;
        }
    }
}
