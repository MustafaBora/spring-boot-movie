package org.hyf.movie.security;


import lombok.RequiredArgsConstructor;
import org.hyf.movie.filters.JwtAuthenticationFilter;
import org.hyf.movie.filters.LoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity  // This annotation is for enabling Spring Security's web security support and provides the Spring MVC integration.
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final LoggingFilter loggingFilter;

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
            //tell which endpoints need to have which authorization (roles)
            //which endpoints are OK to be reached even without authentiaction etc.
                .authorizeHttpRequests(auth ->
                    auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()  //TODO make it admin or user
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/movies/**").hasRole("ADMIN")
                        .anyRequest().authenticated()   //you dont have to be admin but you need to be a user

                    )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(loggingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
