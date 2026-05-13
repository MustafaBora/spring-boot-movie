package org.hyf.movie.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        log.warn(" REQUEST ARRIVED: " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());
        chain.doFilter(request, response);
        log.warn("RESPONSE SENT:" + httpRequest.getRequestURI());
     }
}
