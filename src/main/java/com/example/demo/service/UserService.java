package com.example.demo.service;

import com.example.demo.model.User;
// import java.util.Optional; // Optionalは使わないので削除

public interface UserService {
    
    // 【修正】戻り値から Optional を削除し、実装クラスと型を一致させる
    User findUserById(String userId); 

    // パスワード更新（Controllerから呼ばれる）
    void updatePassword(User user, String newPlainPassword);

    // ... その他の業務ロジック ...
}