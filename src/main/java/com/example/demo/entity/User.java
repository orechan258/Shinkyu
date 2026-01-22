package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * データベースの 'app_user' テーブルとマッピングされるJPAエンティティクラス。
 */
@Entity
@Table(name = "app_user")
public class User {

    // --- DBのカラム ---

    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId; // ユーザーID (業務キー)

    @Column(name = "last_name", nullable = false)
    private String lastName; // 氏名（姓）

    @Column(name = "first_name", nullable = false)
    private String firstName; // 氏名（名）

    @Column(name = "role")
    private String role; // 役職（部長、チーフなど）

    @Column(name = "password", nullable = false, length = 60)
    private String password; // パスワードハッシュ

    @Column(name = "must_change_password")
    private boolean mustChangePassword; // 初回ログイン時強制変更フラグ

    @Column(name = "is_admin")
    private boolean admin; // 管理者フラグ
    
    @Column(name = "is_approver")
    private boolean approver; // 承認者フラグ
    
    @Column(name = "email")
    private String email; // メールアドレス（Google連携用）
    
    @Column(name = "group_id")
    private Integer groupId; // 所属グループID

    // --- 表示用の補助フィールド（DBには保存せずプログラム内でのみ使用） ---

    @Transient
    private String departmentName; 

    @Transient
    private String groupName;

    // --- コンストラクタ ---

    public User() {}

    // --- Getter and Setter ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword; // 無限ループを修正
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}