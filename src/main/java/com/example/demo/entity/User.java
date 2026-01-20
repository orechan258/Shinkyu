package com.example.demo.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_user") // データベースのテーブル名
@Getter
@Setter
public class User {

	@Id
	@Column(name = "user_id", length = 10)
	private String userId;

	@ManyToOne
	@JoinColumn(name = "group_id", nullable = false)
	private Usergroup groupId;

	@Column(name = "last_name", length = 20)
	private String lastName;

	@Column(name = "first_name", length = 20)
	private String firstName;

	@Column(name = "password", length = 60)
	private String password; // BCryptハッシュ値

	@Column(name = "is_approver")
	private Boolean isApprover = false; // 承認者フラグ

	@Column(name = "is_admin")
	private Boolean isAdmin = false; // 管理者フラグ

	@Column(name = "joining_date")
	private LocalDate joiningDate;

	@Column(name = "role", length = 50)
	private String role; // 役割 (例: リーダー, チーフ)

	// --- コンストラクタ ---

	public User() {
		// デフォルトコンストラクタ (JPA必須)
	}

	// --- Spring Securityに必要な権限取得メソッド ---

	/**
	 * Spring Securityが認証後に使用する権限リストを取得します。
	 * 権限フラグに基づいて、"ROLE_" を接頭辞として付与します。
	 */
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();

		// 1. 管理者権限
		if (this.isAdmin != null && this.isAdmin) {
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		}

		// 2. 承認者権限
		if (this.isApprover != null && this.isApprover) {
			authorities.add(new SimpleGrantedAuthority("ROLE_APPROVER"));
		}

		// 3. 全ての認証ユーザーに共通の権限 (必須ではないが便利)
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		return authorities;
	}

	//	// --- Getter と Setter ---
	//
	//	// ID, Group, Name, Password (UserDetailsServiceで使用)
	//	public String getUserId() {
	//		return userId;
	//	}
	//
	//	public void setUserId(String userId) {
	//		this.userId = userId;
	//	}
	//
	//	public Long getGroupId() {
	//		return groupId;
	//	}
	//
	//	public void setGroupId(Long groupId) {
	//		this.groupId = groupId;
	//	}
	//
	//	public String getLastName() {
	//		return lastName;
	//	}
	//
	//	public void setLastName(String lastName) {
	//		this.lastName = lastName;
	//	}
	//
	//	public String getFirstName() {
	//		return firstName;
	//	}
	//
	//	public void setFirstName(String firstName) {
	//		this.firstName = firstName;
	//	}
	//
	//	public String getPassword() {
	//		return password;
	//	}
	//
	//	public void setPassword(String password) {
	//		this.password = password;
	//	}
	//
	//	// 権限フラグ (トグル処理で使用)
	//	public Boolean getIsApprover() {
	//		return isApprover;
	//	}
	//
	//	public void setIsApprover(Boolean isApprover) {
	//		this.isApprover = isApprover;
	//	}
	//
	//	public Boolean getIsAdmin() {
	//		return isAdmin;
	//	}
	//
	//	public void setIsAdmin(Boolean isAdmin) {
	//		this.isAdmin = isAdmin;
	//	}
	//
	//	// その他
	//	public LocalDate getJoiningDate() {
	//		return joiningDate;
	//	}
	//
	//	public void setJoiningDate(LocalDate joiningDate) {
	//		this.joiningDate = joiningDate;
	//	}
	//
	//	public String getRole() {
	//		return role;
	//	}
	//
	//	public void setRole(String role) {
	//		this.role = role;
	//	}
}