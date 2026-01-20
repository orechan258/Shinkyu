package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Request;

@Repository
public interface UsageLogRepository extends JpaRepository<Request, Long> {

	List<Request> findByApply(Integer apply);
	//	List<UsageLog> findByUsageDateBetween(LocalDate start, LocalDate end);
	//
	//	List<UsageLog> findByUsageDateBetweenAndGroupId(LocalDate start, LocalDate end, Integer groupId);
	//
	//	List<UsageLog> findByUsageDateBetweenAndUserId(LocalDate start, LocalDate end, String userId);

}