package com.mitsuru.admin.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Single admin account configured through environment variables.
 * Replace with a JPA-backed users table when registration is added.
 */
@Service
public class CustomUserDetailService implements UserDetailsService{

	private final UserDetails admin;
	private final String adminEmail;

	public CustomUserDetailService(PasswordEncoder encoder,
			@Value("${app.admin.username}") String username,
			@Value("${app.admin.password}") String password,
			@Value("${app.admin.email}") String email) {
		// hash once at startup; BCrypt is deliberately slow
		this.admin = User.withUsername(username)
			.password(encoder.encode(password))
			.roles("ADMIN")
			.build();
		this.adminEmail = email;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		if (!admin.getUsername().equals(username)) {
			throw new UsernameNotFoundException("User not found: " + username);
		}
		// return a copy: Spring Security erases the password on the returned object after login
		return User.withUserDetails(admin).build();
	}

	public String findEmail(String username) {
		loadUserByUsername(username);
		return adminEmail;
	}

}
