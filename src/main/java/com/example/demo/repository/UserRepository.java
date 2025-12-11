package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> { 
    
    // ユーザーID (業務ID) でユーザーを検索 (Spring Security, Service層で使用)
    Optional<User> findByUserId(String userId);

    // ユーザーIDリスト (業務ID) で複数のユーザーを検索 (N+1問題回避に有効)
    List<User> findAllByUserIdIn(List<String> userIds); 
    
    // ★★★ 検索メソッド (JPQL) ★★★
    @Query("SELECT u FROM User u WHERE " +
           // userId, lastName, firstName のいずれかに部分一致する AND 検索を実行
           "LOWER(u.userId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByQuery(@Param("query") String query); 
    
    // ⚠️ findById(int id) は JpaRepository によって自動提供されるため、ここでは省略します。
    // 必要であれば、findById(Integer id); と定義することも可能ですが、int型引数はJPAの規約から外れる場合があります。

    /**
     * パスワードハッシュを更新します。
     * @param id パスワードを更新するユーザーの内部ID (int)
     * @param newHash 新しいパスワードのハッシュ値
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :newHash WHERE u.id = :id")
    void updatePassword(@Param("id") int id, @Param("newHash") String newHash); 

    /**
     * パスワード強制変更フラグの状態を更新します。
     * @param id フラグを更新するユーザーの内部ID (int)
     * @param flag 新しいフラグの状態 (TRUE/FALSE)
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.mustChangePassword = :flag WHERE u.id = :id")
    void updateChangeFlag(@Param("id") int id, @Param("flag") boolean flag);
}