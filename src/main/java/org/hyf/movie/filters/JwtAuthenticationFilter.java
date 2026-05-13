package org.hyf.movie.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hyf.movie.model.User;
import org.hyf.movie.repository.UserRepository;
import org.hyf.movie.security.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // implements Filter

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

//  public void doFilter(ServletRequest req, ServletResponse response,  FilterChain chain) throws ServletException, IOException {
//        HttpServletRequest request = (HttpServletRequest) req;
        String header = request.getHeader("Authorization");


        //Bearer asjdalsdakjsdak.akjshdkasldks.lakjshdkajslkd
        if(header != null && header.startsWith("Bearer ")) {
            String token1 = header.split(" ")[1];
            String token = header.substring(7);


            if(jwtUtils.isValid(token)) {
                String email = jwtUtils.getSubject(token);
                User user = userRepository.findByEmail(email).orElse(null);

                if(user != null) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

                    UsernamePasswordAuthenticationToken authenticationToken
                            = new UsernamePasswordAuthenticationToken(
                            user,       //principal is the logged in user
                            null,
                            List.of(authority)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }

        //always continue with calling other filters
        filterChain.doFilter(request, response);
    }
}
