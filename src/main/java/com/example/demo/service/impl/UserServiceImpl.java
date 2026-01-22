package com.example.demo.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.User; // Modelクラスを短縮名 'User' としてimport
import com.example.demo.repository.UserRepository;
import com.example.demo.security.PasswordService;
import com.example.demo.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserServiceImpl(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * ユーザーID (String) でユーザーを検索し、EntityをModelに変換して返す。
     * Spring Security認証成功後やControllerから参照される、Service層のメインメソッド。
     */
    @Override
    public User findUserById(String userId) {
        // 1. ユーザーID（String）でエンティティを検索
        // Entityは完全修飾名を使用 (com.example.demo.entity.User)
        Optional<com.example.demo.entity.User> userEntityOptional = userRepository.findByUserId(userId);
        
        // 2. エンティティが見つかったらModelに変換して返す
        return userEntityOptional
            .map(this::mapEntityToModel)
            .orElseThrow(() -> new RuntimeException("User not found by ID: " + userId));
    }
    
    /**
     * パスワード更新処理を実行する（トランザクション境界）
     * ⚠️ 注: このメソッドのRepository呼び出しは、業務ID(String)で更新する
     * 新しいカスタムメソッドをUserRepositoryに追加することを強く推奨します。
     */
    @Transactional
    @Override
    public void updatePassword(User user, String newPlainPassword) {
        String newHash = passwordService.hashPassword(newPlainPassword);
        
        // 【重要】
        // 以下の2行は、UserRepositoryに String userId を受け取るカスタムクエリが必要
        // userRepository.updatePassword(user.getUserId(), newHash); 
        // userRepository.updateChangeFlag(user.getUserId(), false); 
        
        // Modelオブジェクトのハッシュ値を更新（画面表示や後続処理のため）
        user.setPassword(newHash); 
    }
    
    /**
     * Entity (DB用) から Model (業務/画面表示用) への変換メソッド
     * Service層の責務として、DBの詳細（Entity）を外部に持ち出さないために使用。
     */
    private User mapEntityToModel(com.example.demo.entity.User userEntity) {
        User userModel = new User(); // Modelをインスタンス化
        
        // データのマッピング
        // ⚠️ Entity側の内部ID(id)は業務キーでないため、Model側で不要ならsetIdは記述しない。
        // userModel.setId(userEntity.getId()); // 内部IDが必要な場合に記述

        userModel.setUserId(userEntity.getUserId());
        userModel.setLastName(userEntity.getLastName());
        userModel.setFirstName(userEntity.getFirstName());
        userModel.setDepartmentName(userEntity.getDepartmentName());
        userModel.setGroupName(userEntity.getGroupName());
        
        // パスワードハッシュは 'password' セッターを使用
        userModel.setPassword(userEntity.getPassword());
        
        // ロール/フラグ情報
        userModel.setMustChangePassword(userEntity.isMustChangePassword());
        userModel.setAdmin(userEntity.isAdmin()); // ModelにsetAdmin/setApproverが必要
        userModel.setApprover(userEntity.isApprover());
        
        return userModel;
    }
}