package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Request;
import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	// Spring Securityで使用
	User findByUserId(String userId);

	// ★★★ 検索メソッドを追加 ★★★
	@Query("SELECT u FROM User u WHERE " +
	// userId, lastName, firstName のいずれかに部分一致する AND 検索を実行 (LOWERで大文字小文字を無視)
			"LOWER(u.userId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
			"LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%'))")
	List<User> searchByQuery(@Param("query") String query);

	List<Request> findByGroupIdIn(List<Long> groupIds);

	List<User> findByUserIdIn(List<String> userIds);
}