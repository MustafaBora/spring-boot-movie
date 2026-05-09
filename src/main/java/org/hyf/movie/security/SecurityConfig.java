package org.hyf.movie.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures Spring Security for the movie API.
 *
 * Route rules (README44 / README47):
 *   /auth/**             — public (register, login)
 *   GET /movies/**       — public (anyone can view movies)
 *   POST/PUT/DELETE /movies/** — ADMIN only
 *   everything else      — must be authenticated
 *
 * CSRF is disabled because we use JWT in the Authorization header,
 * not browser-sent cookies, so the CSRF attack vector does not apply.
 * Sessions are stateless — the server never stores session state.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Registers BCrypt as the password encoder bean.
     * Returning the PasswordEncoder abstraction (DIP) keeps UserService
     * decoupled from the concrete algorithm.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/movies/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
