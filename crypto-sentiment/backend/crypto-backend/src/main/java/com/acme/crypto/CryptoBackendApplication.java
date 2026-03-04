package com.acme.crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoBackendApplication.class, args);
	}

}
