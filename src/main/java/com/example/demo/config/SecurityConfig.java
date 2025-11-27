package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * パスワードエンコーダーのBean定義。
     * ログイン時のパスワード検証や、DBへの保存時に使用する。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoderを使用して、パスワードを安全にハッシュ化する
        return new BCryptPasswordEncoder();
    }

    /**
     * アプリケーション全体のセキュリティ設定（アクセスルール、ログイン/ログアウト設定）を定義する。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 認可（アクセス制御）の設定
            .authorizeHttpRequests(authorize -> authorize
                // 認証なしでアクセス可能 (ログインページと静的リソース)
                .requestMatchers("/", "/login", "/css/**", "/js/**").permitAll() 
                
                // 申請、確認、ホーム画面は、認証されたユーザーならアクセス可能
                .requestMatchers("/request", "/check", "/finish", "/home").authenticated()
                
                // 承認画面は承認者または管理者のロールが必要
                // hasAnyRole() は自動的に "ROLE_" を接頭辞として付与する
                .requestMatchers("/approve", "/approve/**").hasAnyRole("APPROVER", "ADMIN")
                
                // 管理者画面とCSVダウンロードは管理者ロールが必要
                .requestMatchers("/admin", "/csv").hasRole("ADMIN")
                
                // 上記以外で、全ての認証済みユーザーがアクセスできるべきパス
                .anyRequest().authenticated()
            )
            // ログインフォームの設定
            .formLogin(login -> login
                .loginPage("/")               // ログインページのURL (ApplicationControllerで定義済み)
                .loginProcessingUrl("/login") // フォームのPOST送信先（Spring Securityが処理）
                .defaultSuccessUrl("/home", true) // ログイン成功後のリダイレクト先
                .failureUrl("/?error")        // ログイン失敗時のリダイレクト先
                .usernameParameter("userId")  // ユーザー名として使用するフォームパラメータ名
                .passwordParameter("password")// パスワードとして使用するフォームパラメータ名
            )
            // ログアウトの設定
            .logout(logout -> logout
                .logoutSuccessUrl("/")        // ログアウト成功後のリダイレクト先
                .permitAll()
            );

        // ※開発環境ではCSRFを無効化することがあるが、本番環境ではデフォルトで有効にすべき
        // .csrf(AbstractHttpConfigurer::disable); 

        return http.build();
    }
}