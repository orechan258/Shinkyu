package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_group")
public class UserGroup {
    @Id
    private Integer id;
    
    @Column(name = "department_id")
    private Integer departmentId;
    private String name;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}