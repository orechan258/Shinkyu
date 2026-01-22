package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    	System.out.println("OAuth2開始");
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        System.out.println("取得したメール: " + email);

        Optional<User> userOptional = userRepository.findByEmail(email);
        System.out.println("DB検索結果: " + userOptional.isPresent());
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (userOptional.isPresent()) {
            // 【登録済み】既存の権限をセット
            User user = userOptional.get();
            if (user.isAdmin()) authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            if (user.isApprover()) authorities.add(new SimpleGrantedAuthority("ROLE_APPROVER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            // 【未登録】初回紐付け用の暫定権限を付与
            authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
        }

        // Googleから取得した属性（名前やメアド）を保持したまま返す
        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
    }
}