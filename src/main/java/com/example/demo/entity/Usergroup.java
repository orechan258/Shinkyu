package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_group")
@Getter
@Setter

public class Usergroup {
	@Id
	@Column(name = "group_id")
	private Long groupId;

	@Column(name = "name")
	private String groupName;
	@Column(name = "department_id")
	private Integer departmentId;
}
