package com.mitsuru.admin.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.mitsuru.admin.beans.LoginResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	/** @throws org.springframework.security.core.AuthenticationException on bad credentials */
	public LoginResponse login(String username, String password) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(username, password));
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		String token = jwtService.generateToken(userDetails);
		return new LoginResponse(token, JwtService.TOKEN_PREFIX.trim(), jwtService.getExpirationMs() / 1000);
	}

}
