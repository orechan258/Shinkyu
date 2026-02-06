package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.service.impl.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

	// コンストラクタ注入
	public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
			CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
		this.customOAuth2UserService = customOAuth2UserService;
		this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// 1. 認可の設定（上から順に適用されます）
				// SecurityConfig.java
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/login", "/css/**", "/js/**", "/favicon.ico", "/oauth2/**").permitAll()
						// GUEST権限の人が /home にアクセスすることを一時的に許可する（Controllerでリダイレクトさせるため）
						.requestMatchers("/home", "/link-account", "/profile/**")
						.hasAnyRole("GUEST", "USER", "APPROVER", "ADMIN")
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/password/**").authenticated() // パスワード変更画面へのアクセス許可
						.anyRequest().authenticated())

				// 2. 通常のフォームログイン設定
				.formLogin(login -> login
						.loginPage("/") // 自作ログイン画面
						.loginProcessingUrl("/login") // formのaction属性と一致させる
						.successHandler(customAuthenticationSuccessHandler) // ★ここを変更
						.usernameParameter("userId")
						.passwordParameter("password")
						.permitAll())

				// 3. Googleログイン（OAuth2）設定
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/")
						.userInfoEndpoint(userInfo -> userInfo
								.userService(customOAuth2UserService))
						// 成功時はControllerの /home で GUEST かどうかを判定させる
						.defaultSuccessUrl("/home", true))

				// 4. ログアウト設定
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/")
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll());

		return http.build();
	}
}