package com.mitsuru.admin.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mitsuru.admin.beans.LoginRequest;
import com.mitsuru.admin.services.AuthService;

@RestController
public class LoginController {

	@Autowired
	private AuthService authService;

	@RequestMapping("/login")
	public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
		String token = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
		return ResponseEntity.ok(token);
	}
	
}
