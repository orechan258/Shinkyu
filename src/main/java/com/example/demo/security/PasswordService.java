package com.example.demo.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 例：Spring SecurityのBCryptを想定
import org.springframework.stereotype.Service;


@Service
public class PasswordService {

    // 実際のアプリケーションでは、依存性注入（DI）でインスタンス化します
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // 初期パスワードの平文（検証用）
    private static final String INITIAL_PASSWORD_PLAIN = "password";

    // ----------------------------------------------------
    //  ユーティリティクラス（返却値用）
    // ----------------------------------------------------
    public static class ValidationResult {
        public final boolean isValid;
        public final String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }

    // ----------------------------------------------------
    //  セキュリティ機能
    // ----------------------------------------------------
    
    /**
     * パスワードをハッシュ化します。
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * ユーザー登録時に使用する、初期パスワードのハッシュ値を取得します。
     */
    public String getInitialPasswordHash() {
        return hashPassword(INITIAL_PASSWORD_PLAIN);
    }

    // ----------------------------------------------------
    //  【コアロジック】新しいパスワードの検証
    // ----------------------------------------------------
    
    /**
     * ログイン認証時に、平文パスワードとDBのハッシュ値が一致するかをチェックします。
     * @param plainPassword ユーザーが入力した平文のパスワード
     * @param storedHash DBに保存されているハッシュ値
     * @return 一致すればtrue、そうでなければfalse
     */
    public boolean checkPassword(String plainPassword, String storedHash) {
        // BCryptPasswordEncoder の matches メソッドが、ハッシュ比較処理を行います。
        return passwordEncoder.matches(plainPassword, storedHash);
    }
    public ValidationResult validateNewPassword(String newPassword, String currentPasswordHash) {
        
        // 1. 文字数チェック (最低8文字)
        if (newPassword.length() < 8) {
            return new ValidationResult(false, "パスワードは最低8文字以上で設定してください。");
        }

        // 2. 文字種チェック (大文字、小文字、記号)
        // 正規表現は、条件が多い場合は個別に定義すると管理しやすい
        boolean hasUpperCase = newPassword.matches(".*[A-Z].*");
        boolean hasLowerCase = newPassword.matches(".*[a-z].*");
        boolean hasSymbol = newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        if (!hasUpperCase || !hasLowerCase || !hasSymbol) {
            return new ValidationResult(false, "パスワードには大文字、小文字、および記号をそれぞれ1文字以上含める必要があります。");
        }
        
        // 3. 【重要】初期パスワードとの一致チェック（ハッシュ値比較）
        // newPasswordが、currentPasswordHashと一致するか（つまり'password'のままか）をチェック
        if (passwordEncoder.matches(newPassword, currentPasswordHash)) {
            return new ValidationResult(false, "初期パスワード ('password') は安全のため使用できません。");
        }

        // 4. 全てOK
        return new ValidationResult(true, "OK");
    }
}