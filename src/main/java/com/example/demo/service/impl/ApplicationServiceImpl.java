package com.example.demo.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        
        List<Request> allRequests = applicationRepository.findAll();
        
        List<Request> userRequests = allRequests.stream()
                .filter(req -> currentUserId.equals(req.getUserId()))
                .toList();

        List<String> applicantIds = userRequests.stream()
                                                     .map(Request::getUserId)
                                                     .distinct() 
                                                     .toList();
        
        // 【修正】UserRepositoryに定義したfindAllByUserIdInを使用
        Map<String, User> userMap = userRepository.findAllByUserIdIn(applicantIds).stream() 
            .collect(Collectors.toMap(User::getUserId, user -> user));

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
        // 【修正】findByUserIdを使用
        User user = userRepository.findByUserId(userId)
                         .orElseThrow(() -> new RuntimeException("Approver user not found: " + userId));
        // 【修正】Userエンティティに追加したgetGroupId()を使用
        return user.getGroupId(); 
    }
    
    @Override
    public List<Request> findAllRequestsByGroup(String approverUserId) {
        Integer groupId = getApproverGroupId(approverUserId);
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
            UserGroup group = groupMap.get(user.getGroupId()); // user.getGroupId()の呼び出しはここで解決
            Department department = (group != null) ? deptMap.get(group.getDepartmentId()) : null;

            String deptName = (department != null) ? department.getName() : "未所属";
            String groupName = (group != null) ? group.getName() : "未所属";
            
            UserDetailDto dto = new UserDetailDto();
            dto.setUserId(user.getUserId());
            dto.setLastName(user.getLastName());
            dto.setFirstName(user.getFirstName());
            dto.setAdmin(user.isAdmin());
            dto.setApprover(user.isApprover());
            dto.setFullName(user.getLastName() + " " + user.getFirstName());
            dto.setDepartmentName(deptName);
            dto.setGroupName(groupName);

            return dto; 
        }).collect(Collectors.toList());
    }
    
    // 【実装】ApplicationControllerで使用するためにOptionalで返すメソッド
    @Override
    public Optional<UserDetailDto> findUserDetail(String userId) {
         return userRepository.findByUserId(userId)
                 .map(user -> combineUsersWithHierarchy(Collections.singletonList(user)).get(0));
    }
    
    // 【実装】抽象メソッドのfindUserDetailByUserIdを実装 (エラー3対応)
    @Override
    public UserDetailDto findUserDetailByUserId(String userId) {
        // findUserDetailを再利用し、値がなければRuntimeExceptionをスローする
        return findUserDetail(userId)
                 .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
    
    @Override
    @Transactional
    public boolean updatePassword(String userId, String currentPassword, String newPassword) {
        // 【修正】findByUserIdを使用
        User user = userRepository.findByUserId(userId)
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
        // 【修正】findByUserIdを使用
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Boolean currentStatus = user.isApprover(); 
        user.setApprover(currentStatus == null || !currentStatus);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleAdmin(String userId) {
        // 【修正】findByUserIdを使用
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        Boolean currentStatus = user.isAdmin();
        user.setAdmin(currentStatus == null || !currentStatus);
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void updateAllRoles(List<String> approverIds, List<String> adminIds) {
        
        final List<String> finalApproverIds = approverIds != null ? approverIds : Collections.emptyList();
        final List<String> finalAdminIds = adminIds != null ? adminIds : Collections.emptyList();

        String sql = "UPDATE app_user SET is_approver = :isApprover, is_admin = :isAdmin WHERE user_id = :userId";
        
        List<String> allUserIds = userRepository.findAll().stream()
                                           .map(User::getUserId)
                                           .toList();
        
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