package com.example.demo.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.User;
import com.example.demo.security.PasswordService;
import com.example.demo.security.PasswordService.ValidationResult;
import com.example.demo.service.UserService;

/**
 * ユーザー認証とパスワード変更を処理するAPIコントローラー
 */
@RestController("userApiController")
@RequestMapping("/api/user")
public class UserController {

    private final PasswordService passwordService;
    private final UserService userService;
    
    // コンストラクタによる依存注入 (DI)
    @Autowired
    public UserController(PasswordService passwordService, UserService userService) {
        this.passwordService = passwordService;
        this.userService = userService;
    }

    // ----------------------------------------------------
    // API リクエスト/レスポンス DTO (内部クラス)
    // ----------------------------------------------------

    /**
     * 【修正】userIdをintからStringに変更し、業務IDの型に統一
     */
    private record PasswordChangeRequest(String userId, String plainPassword, String newPassword) {}
    private record ApiResponse(boolean success, String message, String redirectPath) {}

    // ----------------------------------------------------
    // ログイン後のリダイレクト判定 API
    // ----------------------------------------------------
    
    @PostMapping("/login-redirect")
    public ResponseEntity<ApiResponse> getLoginRedirect(@RequestBody PasswordChangeRequest request) {
        
        // 1. ユーザー取得: Service層経由でModelを取得（UserServiceはExceptionを投げる設計）
        User user;
        try {
            // 【修正】findUserByIdはStringを引数に取り、Userオブジェクトを直接返す
            user = userService.findUserById(request.userId()); 
        } catch (RuntimeException e) {
            // ユーザーが見つからない場合はここで補足
            return ResponseEntity.badRequest().body(new ApiResponse(false, "ユーザーIDまたはパスワードが正しくありません。", "/login"));
        }

        // 2. 認証処理 (Spring Securityのフィルター外で認証ロジックを再利用)
        if (passwordService.checkPassword(request.plainPassword(), user.getPassword())) {
             // 認証成功
             if (user.isMustChangePassword()) { 
                 return ResponseEntity.ok(new ApiResponse(true, "強制パスワード変更が必要です。", "/change-password-force"));
             } else {
                 return ResponseEntity.ok(new ApiResponse(true, "ログイン成功", "/dashboard"));
             }
        }
        
        // 認証失敗
        return ResponseEntity.badRequest().body(new ApiResponse(false, "ユーザーIDまたはパスワードが正しくありません。", "/login"));
    }

    // ----------------------------------------------------
    // パスワード変更 API
    // ----------------------------------------------------
    
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody PasswordChangeRequest request) {
        
        // 1. ユーザーの現在のレコードを取得
        User user;
        try {
            user = userService.findUserById(request.userId()); 
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "ユーザーが見つかりません。", null));
        }
        
        // 2. 【コアロジック】パスワード検証
        ValidationResult validation = passwordService.validateNewPassword(
             request.newPassword(), 
             user.getPassword() 
        );

        if (!validation.isValid) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, validation.message, null));
        }

        // 3. パスワードの更新処理をService層に委譲
        try {
            // Service層でハッシュ化とDB更新、フラグ解除を一括実行
            userService.updatePassword(user, request.newPassword());
        } catch (Exception e) {
            // DB操作やトランザクションでのエラーを補足
            return ResponseEntity.internalServerError().body(new ApiResponse(false, "更新中にエラーが発生しました。", null));
        }

        return ResponseEntity.ok(new ApiResponse(true, "パスワードが正常に変更されました。", "/dashboard"));
    }
}