package org.example.videoviewer.config.filters;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.exceptions.AuthenticationException;
import org.example.videoviewer.security.jwt.JwtProvider;
import org.example.videoviewer.security.jwt.dto.JwtAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final Set<String> skippedPaths = Set.of("/auth/");

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = extractToken(request);
        if (token != null && validateToken(token, request)) {
            var claims = getClaimsFromToken(token, request);
            var authentication = JwtAuthentication.builder()
                    .username(claims.getSubject())
                    .roles(claims.get("roles", List.class))
                    .authenticated(true)
                    .build();
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return skippedPaths.stream().anyMatch(p -> request.getRequestURI().startsWith(p));
    }

    private String extractToken(HttpServletRequest request) {
        if(isVideoRequested(request)) {
            return Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("STREAM_TOKEN")).findFirst().orElseThrow(() -> new AuthenticationException("No stream token cookie provided")).getValue();
        } else {
            var bearerToken = request.getHeader(AUTHORIZATION_HEADER);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }
        return null;
    }

    private boolean isVideoRequested(HttpServletRequest request) {
        var uri = request.getRequestURI();
        return uri.startsWith("/video/");
    }

    private boolean validateToken(String token, HttpServletRequest request) {
        if(isVideoRequested(request)) {
            return jwtProvider.validateStreamingToken(token);
        } else {
            return jwtProvider.validateAccessToken(token);
        }
    }

    private Claims getClaimsFromToken(String token, HttpServletRequest request) {
        if(isVideoRequested(request)) {
            return jwtProvider.getStreamingClaims(token);
        } else {
            return jwtProvider.getAccessClaims(token);
        }
    }
}
