package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Request;
import com.example.demo.entity.User;

@Repository
public interface AppUserRepository extends JpaRepository<User, String> {

	@Modifying
	@Transactional
	@Query("""
			    SELECT r
			    FROM Request r
			    WHERE r.apply = 1
			      AND r.groupId IN :groupIds
			""")
	List<Request> findApprovedByGroupIds(
			@Param("groupIds") List<Long> groupIds);

	List<User> findByGroupIdIn(List<Long> groupIds);

	Optional<User> findByUserId(String userId);

	@Modifying
	@Transactional
	@Query("""
			    UPDATE User u
			       SET u.groupId = :groupId
			     WHERE u.userId = :userId
			""")
	int updateGroup(
			@Param("userId") String userId,
			@Param("groupId") Long groupId);
}
