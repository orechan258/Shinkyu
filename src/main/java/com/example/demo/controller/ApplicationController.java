package com.example.demo.controller;

import java.security.Principal;
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
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.RequestDetailDto; // ★DTOをインポート

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
            Optional<User> userOptional = userRepository.findById(userId); 

            userOptional.ifPresent(user -> {
                String fullName = user.getLastName() + " " + user.getFirstName();
                model.addAttribute("userName", fullName);
            });
        }
        return "home";
    }
    
    @GetMapping("/finish")
    public String finish() {
        return "request_finish";
    }
    
    // --- 申請機能 (/request) ---
    
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
    
    // --- 申請確認機能 (/check) - DTO利用で氏名表示 ---
    
    @GetMapping("/check")
    public String checkRequest(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/"; 
        }
        
        String currentUserId = principal.getName();
        
        // ★Serviceから氏名付きのDTOリストを取得
        List<RequestDetailDto> allUserRequestsWithNames = applicationService.findMyRequestsWithNames(currentUserId);
        
        // DTOリストを分割
        List<RequestDetailDto> pendingRequests = allUserRequestsWithNames.stream()
                .filter(req -> req.getApply() == null || req.getApply() == 0)
                .toList();

        List<RequestDetailDto> completedRequests = allUserRequestsWithNames.stream()
                .filter(req -> req.getApply() != null && (req.getApply() == 1 || req.getApply() == 2))
                .toList();

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("completedRequests", completedRequests);
        
        return "check";
    }
    
    // --- 承認機能 (/approve) ---
    
    @GetMapping("/approve")
    public String displayApplications(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/";
        }
        String approverUserId = principal.getName();
        
        // グループでフィルタリングされた申請リストを取得
        List<Request> allRequests = applicationService.findAllRequestsByGroup(approverUserId); 

        // 特認ソート
        allRequests.sort(
            Comparator.comparing(
                Request::getSpApply, 
                Comparator.nullsLast(Boolean::compareTo).reversed()
            )
        );

        List<Request> pendingRequests = allRequests.stream()
                .filter(req -> req.getApply() == null || req.getApply() == 0)
                .collect(Collectors.toList());

        List<Request> approvedRequests = allRequests.stream()
                .filter(req -> req.getApply() != null && req.getApply() >= 1) 
                .collect(Collectors.toList());

        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("approvedRequests", approvedRequests);

        return "approve"; 
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
        List<User> list;
        
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
    
    @PostMapping("/admin/roles/save") // 権限の一括保存機能
    public String saveAllRoles(
            @RequestParam(name = "approverStatus", required = false) List<String> approverList,
            @RequestParam(name = "adminStatus", required = false) List<String> adminList) {
        
        applicationService.updateAllRoles(approverList, adminList);

        return "redirect:/admin/roles";
    }
}