package com.example.demo;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashVerifier {
    public static void main(String[] args) {
        // ❌ フォームに入力した平文のパスワード (例: testpassword)
        String inputPassword = "test"; 
        
        // ✅ DBから取得した完全なハッシュ値（60文字）
        String dbHash = "$2a$10$LWh8xeKI7Bn6DAoOtAD1Vu2/xtuSPfseZTd7AzxdXtCg16PKejceG"; 
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 【重要】matches()メソッドが内部で「ユーザー提供ハッシュ」を生成し、「DB格納ハッシュ」と比較します。
        boolean matches = encoder.matches(inputPassword, dbHash);
        
        System.out.println("入力パスワード: " + inputPassword);
        System.out.println("DBハッシュ:     " + dbHash);
        System.out.println("-------------------------------------");
        System.out.println("比較結果 (matches): " + matches);
    }
}