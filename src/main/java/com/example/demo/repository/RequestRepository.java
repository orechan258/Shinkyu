package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {

	//	// =========================
	//	// ① 会社全体
	//	// =========================
	//	@Query("""
	//			    SELECT
	//			        r.userId AS userId,
	//			        FUNCTION('DAYOFWEEK', r.startDate) AS dayOfWeek,
	//			        CASE
	//			            WHEN r.startTime < :noon THEN 'AM'
	//			            ELSE 'PM'
	//			        END AS timeSlot,
	//			        COUNT(r) AS requestCount
	//			    FROM Request r
	//			    WHERE r.apply = 1
	//			      AND r.startDate BETWEEN :start AND :end
	//			    GROUP BY
	//			        r.userId,
	//			        FUNCTION('DAYOFWEEK', r.startDate),
	//			        CASE
	//			            WHEN r.startTime < :noon THEN 'AM'
	//			            ELSE 'PM'
	//			        END
	//			""")
	//	List<RequestUsageSummary> aggregateCompany(
	//			@Param("start") LocalDate start,
	//			@Param("end") LocalDate end,
	//			@Param("noon") LocalTime noon);
	//
	//	// =========================
	//	// ② 個人
	//	// =========================
	//	@Query("""
	//			    SELECT
	//			        r.userId AS userId,
	//			        FUNCTION('DAYOFWEEK', r.startDate) AS dayOfWeek,
	//			        CASE
	//			            WHEN r.startTime < :noon THEN 'AM'
	//			            ELSE 'PM'
	//			        END AS timeSlot,
	//			        COUNT(r) AS requestCount
	//			    FROM Request r
	//			    WHERE r.apply = 1
	//			      AND r.userId = :userId
	//			      AND r.startDate BETWEEN :start AND :end
	//			    GROUP BY
	//			        r.userId,
	//			        FUNCTION('DAYOFWEEK', r.startDate),
	//			        CASE
	//			            WHEN r.startTime < :noon THEN 'AM'
	//			            ELSE 'PM'
	//			        END
	//			""")
	//	List<RequestUsageSummary> aggregateByUser(
	//			@Param("userId") String userId,
	//			@Param("start") LocalDate start,
	//			@Param("end") LocalDate end,
	//			@Param("noon") LocalTime noon);
	//
	//	// =========================
	//	// ③ グループ
	//	// =========================
	//	@Query("""
	//			    SELECT
	//			        r.userId AS userId,
	//			        FUNCTION('DAYOFWEEK', r.startDate) AS dayOfWeek,
	//			        CASE
	//			            WHEN r.startTime < :noon THEN 'AM'
	//			            ELSE 'PM'
	//			        END AS timeSlot,
	//			        COUNT(r) AS requestCount
	//			    FROM Request r
	//			    WHERE r.apply = 1
	//			      AND r.groupId = :groupId
	//			      AND r.startDate BETWEEN :start AND :end
	//			    GROUP BY
	//			        r.userId,
	//			        FUNCTION('DAYOFWEEK', r.startDate),
	//			        CASE
	//			            WHEN r.startTime < :noon THEN 'AM'
	//			            ELSE 'PM'
	//			        END
	//			""")
	//	List<RequestUsageSummary> aggregateByGroup(
	//			@Param("groupId") Long groupId,
	//			@Param("start") LocalDate start,
	//			@Param("end") LocalDate end,
	//			@Param("noon") LocalTime noon);

	@Modifying
	@Transactional
	@Query(value = """
			  UPDATE request r
			  JOIN app_user u ON r.user_id = u.user_id
			  SET r.group_id = u.group_id
			  WHERE r.group_id IS NULL
			""", nativeQuery = true)
	int syncGroupIdFromUser();

	@Modifying
	@Transactional
	@Query("""
			    UPDATE Request r
			    SET r.groupId = :newGroupId
			    WHERE r.userId = :userId
			""")
	int resyncGroupIdByUser(
			@Param("userId") String userId,
			@Param("newGroupId") Long newGroupId);

	// 全社（グループ指定なし）＋ apply = 1
	List<Request> findByApply(Integer apply);

	// グループ指定あり ＋ apply = 1
	List<Request> findByGroupIdInAndApply(List<Long> groupIds, Integer apply);

	List<Request> findByGroupIdIn(List<Long> groupIds);

}
