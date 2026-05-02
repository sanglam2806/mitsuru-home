package com.mitsuru.admin.services;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;


@Service
public class JwtService {

	public static final String SECRET = "TimMitsukeru";
	public static final long EXPIRATION_TIME = 864_000_000; // 10 days
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	public static final String SIGN_UP_URL = "/api/user/create";

	public String extractUsername(String token) {
		String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
		.build()
		.verify(token.replace(TOKEN_PREFIX, ""))
		.getSubject();
		return user;
	}

	public String generateToken(UserDetails userDetails){
		String token = JWT.create()
		.withSubject(userDetails.getUsername())
		.withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
		.sign(Algorithm.HMAC512(SECRET.getBytes()));
		return token;
	}

	
}
