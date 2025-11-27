package com.example.demo; // 既存のパッケージ内でもOKです

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password"; // ★あなたがログイン時に使いたいパスワード
        
        // BCryptアルゴリズムでハッシュ化
        String hashedPassword = encoder.encode(rawPassword); 
        
        // 生成されたハッシュ値をコンソールに出力
        System.out.println("-------------------------------------------------");
        System.out.println("元のパスワード: " + rawPassword);
        System.out.println("BCryptハッシュ値: " + hashedPassword);
        System.out.println("-------------------------------------------------");
    }
}