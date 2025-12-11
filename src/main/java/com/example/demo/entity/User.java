package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity; // Spring Boot 3.x以降は jakarta.persistence を使用
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * データベースの 'user' テーブルとマッピングされるJPAエンティティクラス。
 */
@Entity
@Table(name = "app_user") // データベースのテーブル名
public class User {


    // --- DBのカラム ---
	@Id
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId; // ユーザーID (業務キー)

    @Column(name = "last_name", nullable = false)
    private String lastName; // 氏名（姓）

    @Column(name = "first_name", nullable = false)
    private String firstName; // 氏名（名）

    @Column(name = "department_name")
    private String departmentName; // 所属部署

    @Column(name = "group_name")
    private String groupName; // 所属グループ

    @Column(name = "password", nullable = false, length = 60) // カラム名を 'password' に変更
    private String password;

    @Column(name = "must_change_password")
    private boolean mustChangePassword; // 初回ログイン時強制変更フラグ
 // 【追加】権限を示すDBカラムを想定したフィールド
    @Column(name = "is_admin")
    private boolean admin; // 管理者フラグ
    
    @Column(name = "is_approver")
    private boolean approver; // 承認者フラグ
    
    private Integer groupId; // 所属グループID
    
    // --- コンストラクタ（JPAは引数なしのコンストラクタを要求します） ---
    public User() {}

    // --- Getter and Setter ---



    // ユーザーID
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // 氏名（姓）
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // 氏名（名）
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // 所属部署
    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // 所属グループ
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    // パスワードハッシュ
    public String getPassword() { // Spring Securityの規約 (UserDetails) に合わせる
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // 強制変更フラグ (boolean型のGetter)
    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
    
 // 【追加】UserDetailsServiceImplで参照されるゲッター
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isApprover() {
        return approver;
    }

    public void setApprover(boolean approver) {
        this.approver = approver;
    }
    
    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}