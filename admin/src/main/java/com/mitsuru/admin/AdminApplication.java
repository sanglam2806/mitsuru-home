package com.mitsuru.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
		printHello();
	}
	private static void printHello() {
		System.out.println("Hello Na-chan");
	}

}
