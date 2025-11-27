package com.example.demo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ログイン処理時にSpring Securityが呼び出すメソッド
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        
        // データベースからユーザーIDで検索
        User user = userRepository.findByUserId(userId);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }

        // Spring Securityが認識できる UserDetails オブジェクトを作成
        // ロール（権限）は今回は空 (Collections.emptyList()) としますが、後でisApproverやisAdminを使って実装します。
        return new org.springframework.security.core.userdetails.User(
        	    user.getUserId(),
        	    user.getPassword(),
        	    // ★修正: 以前は Collections.emptyList() だった部分を、Entityから取得した権限リストに置き換える
        	    user.getAuthorities() 
        	);
    }
}