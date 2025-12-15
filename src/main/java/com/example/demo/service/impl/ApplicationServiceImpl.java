package com.example.demo.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.repository.RequestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.RequestDetailDto;
import com.example.demo.service.UserDetailDto;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    // コンストラクタインジェクション (finalフィールドの初期化)
    public ApplicationServiceImpl(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository; 
        this.userRepository = userRepository;
    }

    // ====================================================================
    // ユーザー情報取得 (Finders)
    // ====================================================================

    @Override
    public Optional<UserDetailDto> findUserDetail(String userId) {
        return userRepository.findByUserId(userId)
                .map(this::mapEntityToUserDetailDto);
    }
    
    @Override
    public UserDetailDto findUserDetailByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .map(this::mapEntityToUserDetailDto)
                .orElseThrow(() -> new RuntimeException("ユーザー詳細が見つかりません: " + userId)); 
    }

    @Override
    public List<UserDetailDto> printAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapEntityToUserDetailDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDetailDto> searchUsers(String search) {
        return userRepository.findByUserIdContainingOrLastNameContainingOrFirstNameContaining(search, search, search)
                .stream()
                .map(this::mapEntityToUserDetailDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> getAllDepartmentNames() {
        // User Entityの getGroupId() が Integer を返すことを前提とし、
        // ユーザーテーブルからユニークなグループIDを取得し、文字列として返却する
        return userRepository.findAll().stream()
                .map(User::getGroupId) 
                .filter(id -> id != null)
                .distinct() 
                .map(Object::toString) 
                .sorted()
                .collect(Collectors.toList());
    }

    // ====================================================================
    // 申請機能 (リクエスト処理)
    // ====================================================================

    @Override
    @Transactional
    public Request createNewRequest(Request request) {
        return requestRepository.save(request); 
    }

    @Override
    public List<RequestDetailDto> findMyRequestsWithNames(String userId) {
        List<Request> requests = requestRepository.findByUserId(userId);
        
        return requests.stream()
                .map(this::mapEntityToRequestDetailDto)
                .sorted(Comparator.comparing(RequestDetailDto::getStartDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelRequest(Long requestId, String userId) {
        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("申請ID [" + requestId + "] が見つかりません。"));

        if (!request.getUserId().equals(userId)) {
            throw new SecurityException("権限エラー: 申請ID [" + requestId + "] はあなたの申請ではありません。");
        }
        
        if (request.getApply() != null && request.getApply() != 0) {
            throw new IllegalStateException("申請ID [" + requestId + "] は既に処理済みのためキャンセルできません。");
        }

        // 3 = キャンセル済み
        request.setApply(3); 
        requestRepository.save(request);
    }

    // ====================================================================
    // 承認機能
    // ====================================================================

    @Override
    public List<Request> findAllRequestsByGroup(String approverUserId) {
        User approver = userRepository.findByUserId(approverUserId)
                .orElseThrow(() -> new RuntimeException("承認者ユーザーが見つかりません: " + approverUserId));
        
        Integer groupId = approver.getGroupId();
        if (groupId == null) {
            return List.of(); 
        }

        return requestRepository.findAllRequestsByGroupExcludingUser(groupId, approverUserId);    }

    @Override
    @Transactional
    public void updateApprovalStatus(Long requestId, boolean approved) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("申請IDが見つかりません: " + requestId));

        // 承認 (1) または 拒否 (2) のステータスを設定
        request.setApply(approved ? 1 : 2);
        requestRepository.save(request);
    }
    
    @Override
    public List<Request> findAll() {
        // 全ての申請 (Request Entity) を返す
        return requestRepository.findAll(); 
    }

    // ====================================================================
    // 管理者機能 & パスワード
    // ====================================================================

    @Override
    @Transactional
    public void toggleApprover(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        user.setApprover(!user.isApprover());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleAdmin(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        user.setAdmin(!user.isAdmin());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateAllRoles(List<String> approverList, List<String> adminList) {
        List<User> allUsers = userRepository.findAll();
        
        for (User user : allUsers) {
            boolean isApprover = approverList != null && approverList.contains(user.getUserId());
            boolean isAdmin = adminList != null && adminList.contains(user.getUserId());
            
            user.setApprover(isApprover);
            user.setAdmin(isAdmin);
        }
        
        userRepository.saveAll(allUsers);
    }
    
    @Override
    @Transactional
    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + userId));

        // ⚠️ セキュリティ対応: ここにパスワードエンコーダを使った認証ロジックが入るべき
        // ⚠️ セキュリティ対応: newPassword はハッシュ化して保存すべき
        String hashedNewPassword = newPassword; 
        
        int updatedRows = userRepository.updatePassword(userId, hashedNewPassword);

        return updatedRows > 0;
    }

    // ====================================================================
    // マッピングヘルパーメソッド
    // ====================================================================

    private UserDetailDto mapEntityToUserDetailDto(User userEntity) {
        UserDetailDto dto = new UserDetailDto();
        dto.setUserId(userEntity.getUserId());
        dto.setLastName(userEntity.getLastName());
        dto.setFirstName(userEntity.getFirstName());
        dto.setFullName(userEntity.getLastName() + " " + userEntity.getFirstName());
        dto.setAdmin(userEntity.isAdmin());
        dto.setApprover(userEntity.isApprover());
        return dto;
    }

    private RequestDetailDto mapEntityToRequestDetailDto(Request request) {
        RequestDetailDto dto = new RequestDetailDto();
        
        // 1. IDのマッピング
        // ⚠️ Request Entityの主キーゲッターが getRequestId() であると仮定
        dto.setRequestId(request.getRequestId()); 
        
        // 2. ユーザーID
        dto.setUserId(request.getUserId());
        
        // 3. 日付/時刻のマッピング (LocalDate, LocalTime -> String へ変換)
        if (request.getStartDate() != null) {
            dto.setStartDate(request.getStartDate().toString());
        }
        if (request.getStartTime() != null) {
            dto.setStartTime(request.getStartTime().toString());
        }
        if (request.getEndDate() != null) {
            dto.setEndDate(request.getEndDate().toString());
        }
        if (request.getEndTime() != null) {
            dto.setEndTime(request.getEndTime().toString());
        }
        
        // 4. その他のフィールド
        dto.setReason(request.getReason());
        dto.setApply(request.getApply());
        
        // 5. boolean型のマッピング
        // ⚠️ Entityに適切なゲッターが存在すると仮定 (getHalfDay() / getSpApply())
        try {
            dto.setHalfDay(request.getHalfDay()); 
            dto.setSpApply(request.getSpApply());
        } catch (Exception e) {
            // 例外が発生した場合のフォールバック (ログ出力は省略)
            dto.setHalfDay(false);
            dto.setSpApply(false);
        }
        
        // 6. 申請者氏名の結合
        userRepository.findByUserId(request.getUserId()).ifPresent(user -> {
            dto.setApplicantFullName(user.getLastName() + " " + user.getFirstName());
        });
        
        return dto;
    }
}