package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Request;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> { // 主キーの型がLongと仮定

    /**
     * 申請者ユーザーID (String) に基づいて、該当ユーザーの全ての申請を取得する
     */
    List<Request> findByUserId(String userId); 

    /**
     * 【JPQLで修正】承認者と同じグループに属するユーザーの申請を取得する
     * Request EntityにはgroupIdがないため、User EntityとJOINするカスタムクエリを使用。
     * 自身の申請 (r.userId != :userId) は除外する。
     *
     * @param groupId 承認者のグループID
     * @param userId 承認者自身のユーザーID (自身の申請を除外するため)
     */
    @Query("SELECT r FROM Request r JOIN User u ON r.userId = u.userId WHERE u.groupId = :groupId AND r.userId != :userId AND r.Apply = 0")
    List<Request> findAllRequestsByGroupExcludingUser(
        @Param("groupId") Integer groupId, 
        @Param("userId") String userId
    );

    // ⚠️ 以前エラーの原因となっていたメソッドは削除または名称変更が必要です。
    // ❌ List<Request> findByUserGroupIdAndUserIdNot(Integer groupId, String userId); 
}