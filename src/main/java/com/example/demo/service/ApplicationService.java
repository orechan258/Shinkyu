package com.example.demo.service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.entity.Department;
import com.example.demo.entity.ProfileRequest;
import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.entity.UserGroup;


public interface ApplicationService {
    
    // ユーザー情報取得
    Optional<UserDetailDto> findUserDetail(String userId);
    UserDetailDto findUserDetailByUserId(String userId);
    List<UserDetailDto> printAllUsers();
    List<String> getAllDepartmentNames();

    
 // ApplicationService.java に追加
    boolean verifyUser(String userId, String password);
    void updateUserEmail(String userId, String email);
    
    
    // ユーザー情報取得 (ページネーション対応)
    Page<UserDetailDto> findAllUsersPaginated(Pageable pageable);
    
    // 申請機能
    Request createNewRequest(Request request);
    List<RequestDetailDto> findMyRequestsWithNames(String userId);
    void cancelRequest(Long requestId, String userId);

    // 承認機能
    List<Request> findAllRequestsByGroup(String approverUserId);
    void updateApprovalStatus(Long requestId, boolean approved);
    List<Request> findAll();
    
    // 管理者機能
    void toggleApprover(String userId);
    void toggleAdmin(String userId);
    void updateAllRoles(List<String> approverList, List<String> adminList);

    // パスワード機能
    boolean updatePassword(String userId, String oldPassword, String newPassword);
    
 // 【追加】絞り込み用の全グループ情報取得
    List<Department> findAllDepartments(); // ★新規：部署の重複を排除したリスト
    List<UserGroup> findAllGroups(); 

    // 【追加】絞り込み検索用
    Page<UserDetailDto> searchUsersPaginated(String search, List<Integer> groupIds, Pageable pageable);
    
    
 // ★プロフィール申請を保存するための定義を追加
    void saveProfileRequest(ProfileRequest req);

    // ★管理者画面で未承認リストを取得するための定義を追加
    List<ProfileRequest> findPendingProfileRequests();

    // ★承認・却下を実行するための定義を追加
    void approveProfileRequest(Long requestId, boolean approved);
    /**
     * CSVからユーザーを一括登録する
     */
    int importUsersFromCsv(InputStream is) throws Exception;

    /**
     * 個別追加画面からユーザーを登録する
     */
    void registerNewUser(User user);

    /**
     * ユーザーを削除する
     */
    void deleteUser(String userId);
    
}