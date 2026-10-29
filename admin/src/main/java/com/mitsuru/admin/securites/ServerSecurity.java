package com.mitsuru.admin.securites;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mitsuru.admin.services.CustomUserDetailService;

import jakarta.servlet.http.HttpServletResponse;

@EnableWebSecurity
@Configuration
public class ServerSecurity {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) {
		http
			// stateless JWT API with no cookies, so CSRF protection is not needed
			.csrf(csrf -> csrf.disable())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests((request) -> request
				.requestMatchers(HttpMethod.POST, "/login").permitAll()
				// let Spring Boot's /error page through so error responses aren't turned into 401
				.requestMatchers("/error").permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
					response.getWriter().write(
						"{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Missing or invalid access token\"}");
				})
			)
		;

		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider(CustomUserDetailService customUserDetailService,
			PasswordEncoder encoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailService);
		provider.setPasswordEncoder(encoder);

		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
		return config.getAuthenticationManager();
	}
}
