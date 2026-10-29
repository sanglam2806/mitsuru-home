package com.mitsuru.admin.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;


@Service
public class JwtService {

	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";

	private static final int MIN_SECRET_LENGTH = 32;

	private final Algorithm algorithm;
	private final JWTVerifier verifier;
	private final long expirationMs;

	public JwtService(@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
			throw new IllegalStateException("app.jwt.secret (JWT_SECRET) must be at least " + MIN_SECRET_LENGTH + " characters");
		}
		this.algorithm = Algorithm.HMAC512(secret.getBytes(StandardCharsets.UTF_8));
		this.verifier = JWT.require(algorithm).build();
		this.expirationMs = expirationMs;
	}

	/**
	 * Returns the subject of a raw token (without the "Bearer " prefix).
	 *
	 * @throws JWTVerificationException if the signature is wrong or the token has expired
	 */
	public String extractUsername(String token) {
		return verifier.verify(token).getSubject();
	}

	public String generateToken(UserDetails userDetails){
		return JWT.create()
			.withSubject(userDetails.getUsername())
			.withIssuedAt(new Date())
			.withExpiresAt(new Date(System.currentTimeMillis() + expirationMs))
			.sign(algorithm);
	}

	public long getExpirationMs() {
		return expirationMs;
	}

}
