package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User; // User Entityをインポート

@Repository
public interface UserRepository extends JpaRepository<User, String> { // 主キーの型がIntegerと仮定

    /**
     * ログイン認証やユーザー情報取得に使用する、業務ID (String) による検索
     */
    Optional<User> findByUserId(String userId);

    /**
     * 管理者画面でのユーザー検索に使用する
     * ユーザーID、姓、名に対する部分一致検索 (OR条件)
     */
    List<User> findByUserIdContainingOrLastNameContainingOrFirstNameContaining(
            String userId, String lastName, String firstName);
            
    // ----------------------------------------------------
    // 以下はカスタム更新/削除クエリの例 (必要に応じて追加)
    // ----------------------------------------------------

    /**
     * パスワード更新用カスタムクエリ (業務IDを使用)
     * Service層のupdatePasswordメソッドで使用されます。
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :newHash, u.mustChangePassword = false WHERE u.userId = :userId")
    int updatePassword(@Param("userId") String userId, @Param("newHash") String newHash);

    /**
     * パスワード変更必須フラグを更新するカスタムクエリ
     */
    @Modifying
    @Query("UPDATE User u SET u.mustChangePassword = :flag WHERE u.userId = :userId")
    int updateMustChangePasswordFlag(@Param("userId") String userId, @Param("flag") boolean flag);
    
    // ----------------------------------------------------
    // 以下はユーザーのグループID関連のカスタムクエリ (必要に応じて追加)
    // ----------------------------------------------------
    
    /**
     * グループIDによるユーザー検索
     */
    List<User> findByGroupId(Integer groupId);
    
    // ----------------------------------------------------
    // その他、Service層が使用するメソッド
    // ----------------------------------------------------
    
    // findById(Integer) -> JpaRepositoryで提供
    // findAll() -> JpaRepositoryで提供
    // save(User) -> JpaRepositoryで提供
}