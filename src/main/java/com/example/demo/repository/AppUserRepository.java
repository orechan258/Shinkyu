package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;

@Repository
public interface AppUserRepository extends JpaRepository<User, String> {

	/**
	 * 指定されたグループIDに属するユーザーを取得
	 */
	List<User> findByGroupIdIn(List<Integer> groupIds);
}
