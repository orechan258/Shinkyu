package com.example.demo.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Request;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.RequestDetailDto;
import com.example.demo.service.UserDetailDto;

@Controller
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public ApplicationController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    // --- 認証・ホーム関連 ---
    
    @GetMapping("/")
    public String login() {
        return "login";
    }
    
    @GetMapping("/home")
    public String home(Model model, Principal principal){
        if (principal != null) {
            String userId = principal.getName();
            
            Optional<UserDetailDto> userDetailOptional = applicationService.findUserDetail(userId);

            userDetailOptional.ifPresent(dto -> {
                model.addAttribute("userName", dto.getFullName()); 
            });
        }
        return "home";
    }
    
    @GetMapping("/finish")
    public String finish() {
        return "request_finish";
    }
    
    // --- 申請機能 ---
    
    @GetMapping("/request")
    public String request(Model model) {
        model.addAttribute("request", new Request()); 
        return "request";
    }
    
    @PostMapping("/request")
    public String submitRequest(
                Request request,
                Principal principal) {
        
        if (principal != null) {
            request.setUserId(principal.getName());
        } else {
            request.setUserId("GUEST_01"); 
        }
        
        applicationService.createNewRequest(request);

        return "redirect:/finish";
    }
    
    // --- 申請確認機能 (/check) ---
    
    @GetMapping("/check")
    public String checkRequest(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/"; 
        }
        
        // ログインユーザーIDを取得
        String currentUserId = principal.getName(); 
        
        // 氏名結合済みのDTOリストを取得
        List<RequestDetailDto> allUserRequestsWithNames = applicationService.findMyRequestsWithNames(currentUserId);
        
        // 現在時刻を取得
        LocalDateTime now = LocalDateTime.now(); 

        // 1. 承認待ちのリスト (期限切れではない、未確認のもの)
        List<RequestDetailDto> pendingRequests = allUserRequestsWithNames.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && !isExpiredDto(req, now))
                .toList();

        // 2. 確認済みのリスト (承認済みまたは却下されたもの)
        List<RequestDetailDto> completedRequests = allUserRequestsWithNames.stream()
                .filter(req -> req.getApply() != null && (req.getApply() == 1 || req.getApply() == 2))
                .toList();
                
        // 3. 期限切れのリスト (承認待ちステータスだが、終了日時が現在時刻を過ぎているもの)
        List<RequestDetailDto> expiredRequests = allUserRequestsWithNames.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && isExpiredDto(req, now))
                .toList();

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("completedRequests", completedRequests);
        model.addAttribute("expiredRequests", expiredRequests); // HTMLタブ用
        
        return "check";
    }
    
    // 期限切れ判定用のヘルパーメソッド (DTO用)
    private boolean isExpiredDto(RequestDetailDto req, LocalDateTime now) {
        if (req.getEndDate() == null || req.getEndTime() == null) {
            return false;
        }
        try {
            // DB保存形式に合わせてフォーマットを指定 (例: YYYY-MM-DD HH:mm:ss)
            String endDateTimeStr = req.getEndDate() + " " + req.getEndTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr, formatter);
            
            return endDateTime.isBefore(now);
        } catch (Exception e) {
            return false; 
        }
    }
    
    // --- 承認機能 (/approve) ---
    
    @GetMapping("/approve")
    public String displayApplications(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/";
        }
        String approverUserId = principal.getName();
        
        // グループでフィルタリングされた申請リストを取得 (List<Request>)
        List<Request> allRequests = applicationService.findAllRequestsByGroup(approverUserId); 

        // 現在時刻を取得
        LocalDateTime now = LocalDateTime.now(); 

        // 特認ソート: SpApply=Trueを優先
        allRequests.sort(
            Comparator.comparing(
                Request::getSpApply, 
                Comparator.nullsLast(Boolean::compareTo).reversed()
            )
        );

        // 1. 承認待ちのリスト (期限切れではない、未確認のもの)
        List<Request> pendingRequests = allRequests.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && !isExpiredEntity(req, now))
                .collect(Collectors.toList());

        // 2. 確認済み (承認/拒否済み) のリスト
        List<Request> approvedRequests = allRequests.stream()
                .filter(req -> req.getApply() != null && req.getApply() >= 1) 
                .collect(Collectors.toList());
            
        // 3. 期限切れのリスト (承認待ちステータスだが、終了日時が現在時刻を過ぎているもの)
        List<Request> expiredRequests = allRequests.stream()
                .filter(req -> (req.getApply() == null || req.getApply() == 0) && isExpiredEntity(req, now))
                .collect(Collectors.toList());

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("approvedRequests", approvedRequests);
        model.addAttribute("expiredRequests", expiredRequests); // HTMLタブ用

        return "approve"; 
    }
    
    // 期限切れ判定用のヘルパーメソッド (Entity用)
    private boolean isExpiredEntity(Request req, LocalDateTime now) {
        if (req.getEndDate() == null || req.getEndTime() == null) {
            return false;
        }
        try {
            // DB保存形式に合わせてフォーマットを指定 (例: YYYY-MM-DD HH:mm:ss)
            String endDateTimeStr = req.getEndDate() + " " + req.getEndTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr, formatter);
            
            return endDateTime.isBefore(now);
        } catch (Exception e) {
            return false; 
        }
    }
    
    @PostMapping("/approve/action")
    public String handleApprovalAction(
                @RequestParam Long requestId, 
                @RequestParam String action) {
        
        applicationService.updateApprovalStatus(requestId, "approve".equals(action));

        return "redirect:/approve";
    }

    // --- 管理者機能 ---
    
    @GetMapping("/admin")
    public String adminHome(){
        return "admin_home";
    }

    @GetMapping("/admin/roles")
    public String roleHome(Model model, @RequestParam(required = false) String search) {
        List<UserDetailDto> list; 
        
        if (search != null && !search.trim().isEmpty()) {
            list = applicationService.searchUsers(search);
        } else {
            list = applicationService.printAllUsers(); 
        }
        
        model.addAttribute("list", list);
        model.addAttribute("searchQuery", search);
        
        return "role"; 
    }
    
    @PostMapping("/user/toggleApprover")
    public String toggleApprover(@RequestParam("userId") String userId) {
        applicationService.toggleApprover(userId);
        return "redirect:/admin/roles"; 
    }
    
    @PostMapping("/user/toggleAdmin")
    public String toggleAdmin(@RequestParam("userId") String userId) {
        applicationService.toggleAdmin(userId);
        return "redirect:/admin/roles"; 
    }
    
    @PostMapping("/admin/roles/save")
    public String saveAllRoles(
                @RequestParam(name = "approverStatus", required = false) List<String> approverList,
                @RequestParam(name = "adminStatus", required = false) List<String> adminList) {
        
        applicationService.updateAllRoles(approverList, adminList);

        return "redirect:/admin/roles";
    }
}