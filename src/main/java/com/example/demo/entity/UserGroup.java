package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne; // ★追加
import jakarta.persistence.Table;

@Entity
@Table(name = "user_group")
public class UserGroup {
    @Id
    private Integer id;
    
    @Column(name = "department_id", insertable = false, updatable = false) // 外部キーとしてマーク
    private Integer departmentId;
    
    private String name; // グループ名

    // 【追加】Departmentへの関連付け
    @ManyToOne 
    @JoinColumn(name = "department_id") // department_id カラムで結合
    private Department department; 

    // --- ゲッターとセッター ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
}