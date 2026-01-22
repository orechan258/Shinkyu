package com.example.demo.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

/**
 * ユーザー関連の汎用Web画面を処理するコントローラー。
 * パスワード変更などの機能はProfileControllerに委譲し、ルーティング衝突を回避。
 */
@Controller
@RequestMapping("/user-settings") // 衝突回避のため新しいベースパスを使用
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /user-settings
     * ユーザー設定画面を表示する。
     */
    @GetMapping
    public String showUserSettings(Model model, Principal principal) {
        if (principal == null) {
            // 認証済みでなければログイン画面へ
            return "redirect:/login"; 
        }
        
        String currentUserId = principal.getName();
        User user;

        // 【修正】Optionalを削除し、try-catchでUserServiceからのRuntimeExceptionを捕捉
        try {
            // Service層からUser Modelを取得 (RuntimeExceptionの可能性がある)
            user = userService.findUserById(currentUserId);
        } catch (RuntimeException e) {
            // ユーザーが見つからなかった、またはその他のService層エラー
            // ログを出力して、ログインページに戻す
            // e.printStackTrace(); // 開発中のデバッグ用に推奨
            return "redirect:/login?error=notfound";
        }
        
        // ユーザー情報をモデルに追加し、ビューを返す
        model.addAttribute("user", user);
        // user_settings.html テンプレートを返します
        return "user_settings";
    }
    
    // ---------------------------------------------------
    // ⚠️ 衝突回避のため、パスワード更新メソッドは削除しました
    // ---------------------------------------------------
}