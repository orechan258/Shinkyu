package com.example.demo.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Department;
import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.entity.UserGroup;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.UserGroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.RequestDetailDto;
import com.example.demo.service.UserDetailDto;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final JdbcClient jdbcClient;
    private final PasswordEncoder passwordEncoder;

    public ApplicationServiceImpl(
        ApplicationRepository applicationRepository, 
        UserRepository userRepository,
        UserGroupRepository userGroupRepository,
        DepartmentRepository departmentRepository,
        JdbcClient jdbcClient,
        PasswordEncoder passwordEncoder) {
        
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
        this.departmentRepository = departmentRepository;
        this.jdbcClient = jdbcClient;
        this.passwordEncoder = passwordEncoder;
    }
    
    // --- Request 処理 (CRUD) ---
    
    @Override
    public Request createNewRequest(Request request) {
        if (request.getHalfDay() == null) request.setHalfDay(false);
        if (request.getSpApply() == null) request.setSpApply(false);
        if (request.getApply() == null) request.setApply(0);
        return applicationRepository.save(request);
    }

    @Override
    public List<Request> findAll() {
        return applicationRepository.findAll();
    }
    
    @Override
    @Transactional
    public void updateApprovalStatus(Long requestId, boolean isApproved) {
        final Integer newStatus = isApproved ? 1 : 2;
        applicationRepository.findById(requestId).ifPresent(request -> {
            request.setApply(newStatus);
            applicationRepository.save(request);
        });
    }

    // --- 申請者名と結合した申請リストの取得 (check画面用) ---

    @Override
    public List<RequestDetailDto> findMyRequestsWithNames(String currentUserId) {
        
        // 1. 全ての申請データを取得
        List<Request> allRequests = applicationRepository.findAll();
        
        // 2. ログインユーザーの申請のみをフィルタリング
        List<Request> userRequests = allRequests.stream()
                .filter(req -> currentUserId.equals(req.getUserId()))
                .toList();

        // 3. 申請者IDのリストを作成 (Userテーブル検索用。この場合は currentUserId のみ)
        List<String> applicantIds = userRequests.stream()
                                                .map(Request::getUserId)
                                                .distinct() // ユーザーIDはcurrentUserId一つのみ
                                                .toList();
        
        // 4. ユーザー情報を取得し、Mapに格納 (通常は一つのみ)
        Map<String, User> userMap = userRepository.findAllById(applicantIds).stream()
            .collect(Collectors.toMap(User::getUserId, user -> user));

        // 5. RequestをDTOに変換し、氏名を結合
        return userRequests.stream()
            .map(req -> {
                User applicant = userMap.get(req.getUserId());
                String fullName = (applicant != null) 
                                ? applicant.getLastName() + " " + applicant.getFirstName()
                                : "ユーザー情報なし";

                return new RequestDetailDto(req, fullName);
            })
            .toList();
    }

    // --- グループ別申請取得ロジック (閲覧制限) ---

    private Integer getApproverGroupId(String userId) {
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Approver user not found: " + userId));
        return user.getGroupId();
    }
    
    @Override
    public List<Request> findAllRequestsByGroup(String approverUserId) {
        Integer groupId = getApproverGroupId(approverUserId);
        
        // ApplicationRepositoryのカスタムクエリを呼び出してフィルタリング
        return applicationRepository.findByApproverGroupId(groupId); 
    }

    // --- ユーザープロファイル & 階層データ取得 ---
    
    @Override
    public List<String> getAllDepartmentNames() {
        return departmentRepository.findAll().stream()
                .map(Department::getName)
                .collect(Collectors.toList());
    }
    
    private List<UserDetailDto> combineUsersWithHierarchy(List<User> users) {
        // 部署とグループの全データを取得し、Mapに格納 (N+1問題回避)
        Map<Integer, UserGroup> groupMap = userGroupRepository.findAll().stream()
            .collect(Collectors.toMap(UserGroup::getId, group -> group));
            
        Map<Integer, Department> deptMap = departmentRepository.findAll().stream()
            .collect(Collectors.toMap(Department::getId, dept -> dept));

        return users.stream().map(user -> {
            UserGroup group = groupMap.get(user.getGroupId());
            Department department = (group != null) ? deptMap.get(group.getDepartmentId()) : null;

            String deptName = (department != null) ? department.getName() : "未所属";
            String groupName = (group != null) ? group.getName() : "未所属";

            return new UserDetailDto(
                user.getUserId(),
                user.getLastName(),
                user.getFirstName(),
                user.getRole(),
                deptName,
                groupName
            );
        }).collect(Collectors.toList());
    }
    
    @Override
    public UserDetailDto findUserDetailByUserId(String userId) {
        List<User> userList = userRepository.findAllById(Collections.singletonList(userId));
        if (userList.isEmpty()) {
            throw new RuntimeException("User not found: " + userId);
        }
        return combineUsersWithHierarchy(userList).get(0);
    }
    
    @Override
    @Transactional
    public boolean updatePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                       .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    // --- ユーザー関連・権限管理 ---

    @Override
    public List<UserDetailDto> printAllUsers() {
        List<User> allUsers = userRepository.findAll();
        return combineUsersWithHierarchy(allUsers);
    }
    
    @Override
    public List<UserDetailDto> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return printAllUsers();
        }
        List<User> foundUsers = userRepository.searchByQuery(query.trim());
        return combineUsersWithHierarchy(foundUsers);
    }
    
    @Override
    @Transactional
    public void toggleApprover(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Boolean currentStatus = user.getIsApprover();
        user.setIsApprover(currentStatus == null || !currentStatus); 
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleAdmin(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Boolean currentStatus = user.getIsAdmin();
        user.setIsAdmin(currentStatus == null || !currentStatus); 
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void updateAllRoles(List<String> approverIds, List<String> adminIds) {
        
        final List<String> finalApproverIds = approverIds != null ? approverIds : Collections.emptyList();
        final List<String> finalAdminIds = adminIds != null ? adminIds : Collections.emptyList();

        List<String> allUserIds = userRepository.findAll().stream()
                                     .map(User::getUserId)
                                     .toList();
        
        // UPDATE SQLの定義
        String sql = "UPDATE app_user SET is_approver = :isApprover, is_admin = :isAdmin WHERE user_id = :userId";
        
        // 全ユーザーをループし、個別に UPDATE を実行
        for (String userId : allUserIds) {
            int isApprover = finalApproverIds.contains(userId) ? 1 : 0;
            int isAdmin = finalAdminIds.contains(userId) ? 1 : 0;
            
            jdbcClient.sql(sql)
                      .param("isApprover", isApprover)
                      .param("isAdmin", isAdmin)
                      .param("userId", userId)
                      .update();
        }
    }
}