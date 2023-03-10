package com.gnims.project.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.gnims.project.share.message.ExceptionMessage.TOKEN_ERROR;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final String[] PERMIT_URL_PREFIX = {"/social","/auth"};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        for (String prefix : PERMIT_URL_PREFIX) {
            if(request.getRequestURI().startsWith(prefix)) {
                filterChain.doFilter(request,response);
                return;
            }
        }

        String token = jwtUtil.resolveToken(request);

        if(token != null) {
            if(!jwtUtil.validateToken(token)){
                jwtExceptionHandler(response, TOKEN_ERROR, HttpStatus.UNAUTHORIZED.value());
                return;
            }
            Claims info = jwtUtil.getUserInfoFromToken(token);
            setAuthentication(info.getSubject());
        }
        filterChain.doFilter(request,response);
    }

    public void setAuthentication(String nickname) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = jwtUtil.createAuthentication(nickname);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    public void jwtExceptionHandler(HttpServletResponse response, String msg, int statusCode) {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        try {
            String json = new ObjectMapper().writeValueAsString(new SecurityExceptionDto(statusCode, msg));
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}