package com.example.demo.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.RequestDetailDto; // DTOをimport

import jakarta.transaction.Transactional;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JdbcClient jdbcClient;

    public ApplicationServiceImpl(
        ApplicationRepository applicationRepository, 
        UserRepository userRepository,
        JdbcClient jdbcClient) {
        
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jdbcClient = jdbcClient;
    }
    
    // --- Request 処理 (既存) ---
    
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
    public void updateApprovalStatus(Long requestId, boolean isApproved) {
        final Integer newStatus = isApproved ? 1 : 2;
        applicationRepository.findById(requestId).ifPresent(request -> {
            request.setApply(newStatus);
            applicationRepository.save(request);
        });
    }

    // --- グループ別申請取得ロジック (閲覧制限) ---

    // 承認者の所属グループIDを取得するヘルパーメソッド
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

    // --- 申請者名と結合した申請リストの取得 (check画面用) ---

    @Override
    public List<RequestDetailDto> findMyRequestsWithNames(String currentUserId) {
        
        // 1. ログインユーザーの申請のみをフィルタリング
        List<Request> userRequests = applicationRepository.findAll().stream()
                .filter(req -> currentUserId.equals(req.getUserId()))
                .toList();

        // 2. 申請者IDをキーにUser情報を取得し、Mapに格納
        List<String> applicantIds = userRequests.stream().map(Request::getUserId).distinct().toList();
        List<User> users = userRepository.findAllById(applicantIds); 
        Map<String, User> userMap = users.stream()
            .collect(Collectors.toMap(User::getUserId, user -> user));

        // 3. RequestをDTOに変換し、氏名を結合
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


    // --- 権限一括更新 (JdbcClientを使用) ---

    @Override
    @Transactional // ★トランザクションを設定
    public void updateAllRoles(List<String> approverIds, List<String> adminIds) {
        
        final List<String> finalApproverIds = approverIds != null ? approverIds : Collections.emptyList();
        final List<String> finalAdminIds = adminIds != null ? adminIds : Collections.emptyList();

        // 1. 全ユーザーのIDを取得 (JdbcClientでUPDATE対象を探すため)
        List<String> allUserIds = userRepository.findAll().stream()
                                     .map(User::getUserId)
                                     .toList();
        
        // 2. SQL UPDATE文を定義
        String sql = "UPDATE app_user SET is_approver = :isApprover, is_admin = :isAdmin WHERE user_id = :userId";
        
        // 3. 全ユーザーをループし、個別に更新を実行
        for (String userId : allUserIds) {
            // 新しい権限値を決定
            int isApprover = finalApproverIds.contains(userId) ? 1 : 0;
            int isAdmin = finalAdminIds.contains(userId) ? 1 : 0;
            
            // JdbcClientで個別のUPDATEを実行
            jdbcClient.sql(sql)
                      .param("isApprover", isApprover)
                      .param("isAdmin", isAdmin)
                      .param("userId", userId)
                      .update();
        }

        System.out.println("JdbcClient: 権限更新完了 (個別UPDATE/トランザクション内)");
    }
    
    // --- ユーザー関連・権限管理 (既存) ---

    @Override
    public List<User> printAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public void toggleApprover(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Boolean currentStatus = user.getIsApprover();
        user.setIsApprover(currentStatus == null || !currentStatus); 
        userRepository.save(user);
    }

    @Override
    public void toggleAdmin(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Boolean currentStatus = user.getIsAdmin();
        user.setIsAdmin(currentStatus == null || !currentStatus); 
        userRepository.save(user);
    }

    @Override
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findAll();
        }
        String searchPattern = query.trim();
        return userRepository.searchByQuery(searchPattern); 
    }
}