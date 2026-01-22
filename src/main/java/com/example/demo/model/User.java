package com.example.demo.model;

/**
 * 業務ロジックや画面表示に使用するユーザーデータモデル (DTO/Model)
 * DBエンティティと異なり、必要な情報のみを保持し、セッターを持つ。
 */
public class User {

    // --- 業務IDと基本情報 ---
    private String userId;
    private String lastName;
    private String firstName;
    private String departmentName;
    private String groupName;

    // --- 認証・権限情報 ---
    // UserServiceImplで参照・設定されるため必須
    private String password;            // setPassword(String) のため
    private boolean mustChangePassword;
    private boolean admin;              // setAdmin(boolean) のため
    private boolean approver;           // setApprover(boolean) のため
    
    // ----------------------------------------------------------------
    // --- Getter and Setter ---
    // ----------------------------------------------------------------

    // 以下のセッターが、UserServiceImplで参照され、未定義エラーの原因となっています。

    // 【エラー2, 3の解消】setPassword, setAdmin, setApprover を追加

    // 2. setPassword(String)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // 3. setAdmin(boolean)
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    // 3. setApprover(boolean)
    public boolean isApprover() {
        return approver;
    }

    public void setApprover(boolean approver) {
        this.approver = approver;
    }
    
    // --- その他の必須の Getter/Setter ---
    
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

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }
    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}