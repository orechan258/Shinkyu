package com.example.demo.repository; // パッケージは正しい

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Request; // Entity名が Request であることを確認

public interface ApplicationRepository extends JpaRepository<Request, Long> {
	@Query(value = "SELECT r FROM Request r JOIN User u ON r.userId = u.userId WHERE u.groupId = :groupId")
	List<Request> findByApproverGroupId(@Param("groupId") Long groupId);

	List<Request> findByApply(Integer apply);
}