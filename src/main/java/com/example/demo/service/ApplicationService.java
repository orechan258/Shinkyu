package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.Request;

public interface ApplicationService {
    
    // --- 申請 (Request) 関連 ---
    
    /** 全ての申請を取得 */
    List<Request> findAll();
    
    /** 新しい申請を作成・保存 */
    Request createNewRequest(Request request);
    
    /** 承認/拒否ステータスを更新 */
    void updateApprovalStatus(Long requestId, boolean isApproved);
    
    /** ログインユーザーの申請履歴を氏名結合DTOとして取得 (check画面用) */
    List<RequestDetailDto> findMyRequestsWithNames(String currentUserId);
    
    /** 閲覧制限付き: 承認者IDを元に、所属グループの申請のみを取得 (approve画面用) */
    List<Request> findAllRequestsByGroup(String approverUserId); 
    
    /**
     * 【追加】ユーザーIDに基づいて、詳細情報（結合済み氏名など）を持つDTOを取得する。
     * @param userId 検索対象のユーザーID（業務ID）
     * @return UserDetailDtoを持つOptional
     */
    Optional<UserDetailDto> findUserDetail(String userId);

    // --- ユーザー/権限管理 (User/Role) 関連 ---
    
    /** 全ユーザーを階層情報(部署/グループ名)と結合したDTOとして取得 (role画面用) */
    List<UserDetailDto> printAllUsers();
    
    /** 検索クエリに一致するユーザーを階層情報と結合したDTOとして取得 (role画面用) */
    List<UserDetailDto> searchUsers(String query);
    
    /** 管理者権限をトグル */
    void toggleAdmin(String userId);
    
    /** 承認者権限をトグル */
    void toggleApprover(String userId);
    
    /** 権限を一括更新 (JdbcClientで実装) */
    void updateAllRoles(List<String> approverIds, List<String> adminIds);
    
    // --- プロファイル (Profile) 関連 ---
    
    /** ユーザーIDに基づき、階層情報と結合したDTOを取得 */
    UserDetailDto findUserDetailByUserId(String userId);
    
    /** 現在のパスワードを確認し、新しいパスワードで更新 */
    boolean updatePassword(String userId, String currentPassword, String newPassword);
    
    /** 部署の全リスト名を取得 (プルダウン用) */
    List<String> getAllDepartmentNames();
}