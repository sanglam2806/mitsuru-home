package com.mitsuru.admin.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mitsuru.admin.beans.LoginRequest;
import com.mitsuru.admin.beans.LoginResponse;
import com.mitsuru.admin.services.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LoginController {

	private final AuthService authService;

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
		return authService.login(loginRequest.username(), loginRequest.password());
	}

}
