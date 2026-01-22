package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * ログイン認証やユーザー情報取得に使用する
     */
    Optional<User> findByUserId(String userId);
    
    Optional<User> findByEmail(String email);

    /**
     * パスワード更新用カスタムクエリ
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :newHash, u.mustChangePassword = false WHERE u.userId = :userId")
    int updatePassword(@Param("userId") String userId, @Param("newHash") String newHash);

    /**
     * 【最新版】管理者画面での高度な検索・絞り込み
     * 1. 検索ワード（ID、姓、名）による部分一致
     * 2. 複数グループ選択による絞り込み (IN句)
     */
    @Query("SELECT u FROM User u " +
           "WHERE (:search IS NULL OR :search = '' " +
           "       OR u.userId LIKE %:search% " +
           "       OR u.lastName LIKE %:search% " +
           "       OR u.firstName LIKE %:search%) " +
           "AND (:groupIds IS NULL OR u.groupId IN :groupIds)")
    Page<User> findBySearchAndFilters(
            @Param("search") String search,
            @Param("groupIds") List<Integer> groupIds, // 複数選択に対応
            Pageable pageable);

    /**
     * 管理者画面でのユーザー検索（簡易版）
     */
    Page<User> findByUserIdContainingOrLastNameContainingOrFirstNameContaining(
        String userId, String lastName, String firstName, Pageable pageable);
}