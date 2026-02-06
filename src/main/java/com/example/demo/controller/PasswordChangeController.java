package com.example.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.service.ApplicationService;

@Controller
@RequestMapping("/password")
public class PasswordChangeController {

    private final ApplicationService applicationService;

    public PasswordChangeController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/change")
    public String showChangePasswordForm() {
        return "password-change";
    }

    @PostMapping("/change")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes ra) {

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "新しいパスワードと確認用パスワードが一致しません。");
            return "redirect:/password/change";
        }

        // パスワードポリシーチェック
        if (newPassword.length() < 8 || !newPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$")) {
            ra.addFlashAttribute("error", "パスワードは半角英大文字・小文字・数字をすべて含む8文字以上で設定してください。");
            return "redirect:/password/change";
        }

        try {
            boolean success = applicationService.updatePassword(userDetails.getUsername(), currentPassword,
                    newPassword);
            if (success) {
                // updatePasswordメソッド内で成功時にmustChangePasswordフラグをfalseにする修正が必要
                // ApplicationServiceImpl側で対応します
                ra.addFlashAttribute("message", "パスワードを変更しました。");
                return "redirect:/home";
            } else {
                ra.addFlashAttribute("error", "現在のパスワードが間違っています。");
                return "redirect:/password/change";
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "エラーが発生しました: " + e.getMessage());
            return "redirect:/password/change";
        }
    }
}
