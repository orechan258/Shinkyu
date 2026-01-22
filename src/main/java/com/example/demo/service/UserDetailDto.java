package com.example.demo.service;

/**
 * ユーザー詳細情報（ロール情報や結合済み氏名など、画面表示用）を保持するDTO。
 * Entityからの変換や、複数のテーブル情報結合結果をControllerに渡すために使用。
 */
public class UserDetailDto {

    private String userId;
    private String lastName;
    private String firstName;
    
    // 【追加】Controllerの /home で要求された結合済み氏名
    private String fullName; 

    private String departmentName;
    private String groupName;
    
    private String email; // DBから取得した生メアド
    private boolean isGoogleLinked; // emailがnullでなければtrue
    
    // ロール情報（管理画面で使用）
    private boolean admin;
    private boolean approver;
    
    private Integer groupId;

    // --- コンストラクタ ---
    public UserDetailDto() {}

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

    /**
     * 【追加】Controllerのhomeメソッドで使用されます。
     * Service層で '姓 + スペース + 名' の形式で設定されている必要があります。
     */
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
	// これを追加（Getter）
	public boolean isGoogleLinked() {
	    return isGoogleLinked;
	}

	// これを追加（Setter）
	public void setGoogleLinked(boolean googleLinked) {
	    isGoogleLinked = googleLinked;
	}
	
	public String getMaskedEmail() {
	    if (email == null || !email.contains("@")) return null;
	    String[] parts = email.split("@");
	    if (parts[0].length() <= 2) return "**@" + parts[1];
	    return parts[0].substring(0, 2) + "***@" + parts[1];
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
}