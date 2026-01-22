package com.example.demo.controller;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.ProfileRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public ProfileController(ApplicationService applicationService, UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    /**
     * GET /profile
     * プロフィール画面を表示する。
     */
    @GetMapping
    public String showProfile(Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/";

        String userId;
        Object principal = authentication.getPrincipal();

        // 1. ユーザーIDの特定
        if (principal instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            userId = userRepository.findByEmail(email).map(User::getUserId).orElse(null);
        } else {
            // 通常ログインまたは「再認証」後はここを通る
            userId = authentication.getName();
        }

        if (userId == null) return "redirect:/";

        // 2. ★ここが最重要！ モデルに "user" という名前でデータを確実に入れる
        applicationService.findUserDetail(userId).ifPresentOrElse(
            userDto -> model.addAttribute("user", userDto),
            () -> {
                // 万が一DTOが取れなかった時のためのデバッグ用（本来ここには来ないはず）
                System.out.println("DEBUG: UserDetail not found for ID: " + userId);
            }
        );

        // 3. 他のリストもセット
        model.addAttribute("allDepartments", applicationService.findAllDepartments());
        model.addAttribute("departmentList", applicationService.getAllDepartmentNames());
        model.addAttribute("allGroups", applicationService.findAllGroups());

        return "profile";
    }
    /**
     * POST /profile/password
     * パスワード変更処理を実行する。
     * ※Googleログインユーザーはこの機能は使わない（パスワードがないため）が、
     * 通常ログインユーザーのためにロジックを維持。
     */
    @PostMapping("/password")
    public String updatePassword(
            @RequestParam String userId,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {

        // パスワード更新ロジック
        boolean success = applicationService.updatePassword(userId, currentPassword, newPassword);

        if (success) {
            redirectAttributes.addAttribute("success", "password");
            return "redirect:/profile";
        } else {
            redirectAttributes.addFlashAttribute("error", "現在のパスワードが正しくありません。");
            return "redirect:/profile";
        }
    }
    
    @PostMapping("/unlink-google")
    public String unlinkGoogle(Authentication authentication, RedirectAttributes ra) {
        // 1. 現在のユーザーIDを特定
        String userId;
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            userId = userRepository.findByEmail(email).map(User::getUserId).orElse(null);
        } else {
            userId = authentication.getName();
        }

        if (userId != null) {
            // 2. DBのemailをNULLにする
            applicationService.updateUserEmail(userId, null);
            
            // 3. セッション内の認証情報を更新してログインを維持
            // 500エラー防止のため、権限(Authorities)をそのまま引き継ぐ
            UsernamePasswordAuthenticationToken newAuth = 
                new UsernamePasswordAuthenticationToken(userId, null, authentication.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            ra.addFlashAttribute("success", "google_unlinked");
        }
        
        return "redirect:/profile"; 
    }
    
    @GetMapping("/link-google-start")
    public String linkGoogleStart(Authentication authentication, HttpSession session) {
        // 現在のログインIDをセッションにメモしておく
        session.setAttribute("PENDING_LINK_USER_ID", authentication.getName());
        // そのままGoogleの認証へ飛ばす
        return "redirect:/oauth2/authorization/google";
    }
    
    @GetMapping("/applications")
    public String showApplicationsPage(Model model) {
        // 新しいテーブル (profile_request) から未承認分を取得
        List<ProfileRequest> pendingProfileRequests = applicationService.findPendingProfileRequests();
        
        model.addAttribute("pendingRequests", pendingProfileRequests);
        return "admin-applications";
    }
    
    @PostMapping("/application")
    public String submitProfileApplication(
            @RequestParam String userId,
            @RequestParam String newLastName,
            @RequestParam String newFirstName,
            @RequestParam Integer newGroupId,
            RedirectAttributes ra) {

        com.example.demo.entity.ProfileRequest profileRequest = new com.example.demo.entity.ProfileRequest();
        profileRequest.setUserId(userId);
        profileRequest.setNewLastName(newLastName);
        profileRequest.setNewFirstName(newFirstName);
        profileRequest.setNewGroupId(newGroupId);
        profileRequest.setApply(0); // 承認待ち

        applicationService.saveProfileRequest(profileRequest);

        ra.addAttribute("success", "application");
        return "redirect:/profile";
    }
}