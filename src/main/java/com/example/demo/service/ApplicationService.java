package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Request;
import com.example.demo.entity.User;

public interface ApplicationService {

	// Request CRUD
	List<Request> findAll();

	Request createNewRequest(Request request);

	void updateApprovalStatus(Long requestId, boolean isApproved);

	// ★承認者IDを元に、所属グループの申請のみを取得 (閲覧制限)
	List<Request> findAllRequestsByGroup(String approverUserId);

	// User/Role Management
	List<User> printAllUsers();

	void toggleAdmin(String userId);

	void toggleApprover(String userId);

	List<User> searchUsers(String query);

	// ★権限一括更新 (JdbcClientで実装)
	void updateAllRoles(List<String> approverIds, List<String> adminIds);

	List<RequestDetailDto> findMyRequestsWithNames(String currentUserId);

}