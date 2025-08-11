package org.example.videoviewer.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.utils.jwt.JwtProvider;
import org.example.videoviewer.utils.jwt.dto.JwtAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtProvider jwtProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var token = extractToken((HttpServletRequest) servletRequest);
        if (token != null && jwtProvider.validateAccessToken(token)) {
            var claims = jwtProvider.getAccessClaims(token);
            var authentication = JwtAuthentication.builder()
                    .username(claims.getSubject())
                    .roles(claims.get("roles", Set.class))
                    .authenticated(true)
                    .build();
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
