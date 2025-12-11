package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    /**
     * ログイン処理時にSpring Securityが呼び出すメソッド
     */
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        
        // 1. データベースからユーザーIDで検索 (Optionalを使用)
        // ⚠️ userRepositoryのメソッドは findByUserId が必要
        Optional<User> userOptional = userRepository.findByUserId(userId); 

        // 2. ユーザーが見つからない場合の例外処理
        User user = userOptional.orElseThrow(() -> 
            new UsernameNotFoundException("User not found with userId: " + userId));
        
      //test
        System.out.println(user.getPassword());

     // 3. 【重要】権限（Authorities/Role）リストを作成
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // ユーザーエンティティのゲッターが定義されたため、エラーなく動作する
        if (user.isAdmin()) { 
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (user.isApprover()) { 
            authorities.add(new SimpleGrantedAuthority("ROLE_APPROVER"));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        

        // 4. Spring Securityが認識できる UserDetails オブジェクトを作成
        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),          
                user.getPassword(),    
                authorities                
            );
    }
}