package com.example.demo.controller; // 適切なパッケージ名に合わせてください

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.User;

import lombok.Data;

/**
 * 画面から複数のユーザー情報を一括で受け取るためのフォームクラス
 */
@Data
public class UserForm {
    
    // HTMLの name="userList[0].userId" などと対応します
    private List<User> userList;

    /**
     * デフォルトコンストラクタ
     * リストを初期化しておくと、Springがバインドする際に
     * NullPointerExceptionを防ぎやすくなります。
     */
    public UserForm() {
        this.userList = new ArrayList<>();
    }
}