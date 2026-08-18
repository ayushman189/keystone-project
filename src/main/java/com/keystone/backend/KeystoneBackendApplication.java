package com.keystone.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KeystoneBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(KeystoneBackendApplication.class, args);
	}

}
