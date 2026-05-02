package com.mitsuru.admin.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private CustomUserDetailService customUserDetailService;
	@Autowired
	private JwtService jwtService;

	public String login(String username, String password) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		UserDetails userDetails = customUserDetailService.loadUserByUsername(username); 

		return jwtService.generateToken(userDetails);
	}

	
}
