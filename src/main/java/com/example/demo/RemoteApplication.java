package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration; // 削除
// import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration; // 削除

@SpringBootApplication // exclude 属性を削除
public class RemoteApplication {
	

	public static void main(String[] args) {
		SpringApplication.run(RemoteApplication.class, args);
	}

}