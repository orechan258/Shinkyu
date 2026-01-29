package com.example.demo.service;

import java.util.List;
import java.util.Map;

public interface UserTransferService {

	Map<String, Long> getApprovedUserGroupMap(List<Integer> groupIds);
}
