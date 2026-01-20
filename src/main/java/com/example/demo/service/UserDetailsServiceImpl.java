package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// ログイン処理時にSpring Securityが呼び出すメソッド

	@Override
	public UserDetails loadUserByUsername(String userId)
			throws UsernameNotFoundException {

		System.out.println("★★ loadUserByUsername 呼ばれた: " + userId);

		User user = userRepository.findByUserId(userId)
				.orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + userId));

		// 権限リスト作成
		List<String> roles = new ArrayList<>();
		roles.add("USER"); // 全員共通

		if (Boolean.TRUE.equals(user.getIsAdmin())) {
			roles.add("ADMIN");
		}

		if (Boolean.TRUE.equals(user.getIsApprover())) {
			roles.add("APPROVER");
		}

		System.out.println("付与される権限: " + roles);

		return org.springframework.security.core.userdetails.User
				.withUsername(user.getUserId())
				.password(user.getPassword())
				.roles(roles.toArray(new String[0])) // ROLE_ は自動付与
				.build();
	}
}