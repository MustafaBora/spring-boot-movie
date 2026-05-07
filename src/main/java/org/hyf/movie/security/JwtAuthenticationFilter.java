package org.hyf.movie.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.hyf.movie.model.User;
import org.hyf.movie.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Runs on every HTTP request before the controller.
 *
 * If the request carries a valid "Authorization: Bearer <token>" header,
 * this filter identifies the user and stores them in the SecurityContext.
 * If the token is missing or invalid, the filter does nothing and lets
 * Spring Security's route rules decide whether to reject the request.
 */
@Component
public class JwtAuthenticationFilter implements Filter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    // This method is called for every HTTP request. It checks for a Bearer token,
    // validates it, and if valid, loads the user and sets the authentication.
    // if not valid, it simply continues without setting authentication, allowing public routes to work.
    // if we have returned a 401 Unothorized from the filter, we would have to stop the chain and not call chain.doFilter(),
    // but we have public routes that should work even without authentication, so we don't want to block the request here.
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // remove "Bearer " prefix

            if (jwtUtils.isValid(token)) {
                String email = jwtUtils.getSubject(token);
                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    // Prefix the role with "ROLE_" so that hasRole("ADMIN") works correctly
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    // if the user had multiple roles, we would create a list of authorities here instead of just one
                    // the code would look like this:
                    // List<GrantedAuthority> authorities = user.getRoles().stream()
                    //         .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    //         .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    user,          // principal — the logged-in User
                                    null,          // credentials — not needed after authentication
                                    List.of(authority)
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        // Always continue — public routes work even without a token
        chain.doFilter(req, res);
    }
}
