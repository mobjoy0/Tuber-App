package com.project.Tuber_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication
public class TuberBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(TuberBackendApplication.class, args);
		System.out.println("APP STARTED");
	}

}
