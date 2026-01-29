package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {

	// 全社（グループ指定なし）＋ apply = 1
	@Query("select r from Request r where r.apply = 1")
	List<Request> findApproved();

	List<Request> findByApply(Integer apply);
}
