package org.example.videoviewer.controllers;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import jdk.jshell.spi.ExecutionControl;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.controllers.dto.JwtResponseDTO;
import org.example.videoviewer.security.jwt.AuthService;
import org.example.videoviewer.security.jwt.dto.JwtRefreshRequest;
import org.example.videoviewer.security.jwt.dto.JwtRequest;
import org.example.videoviewer.security.jwt.dto.OneTimeCodeRequest;
import org.example.videoviewer.security.jwt.dto.PasswordResetRequest;
import org.example.videoviewer.security.jwt.dto.RegistrationRequest;
import org.example.videoviewer.security.jwt.dto.RequestPasswordResetRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${application.cookies.ttl-minutes}") private Integer cookieTtlMinutes;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegistrationRequest registrationRequest) throws MessagingException, FileNotFoundException {
        authService.register(registrationRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody JwtRequest loginRequest) throws MessagingException, FileNotFoundException {
        authService.login(loginRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/exchange-one-time-code")
    public ResponseEntity<JwtResponseDTO> exchangeOneTimeCode(@RequestBody OneTimeCodeRequest oneTimeCodeRequest/*, HttpServletResponse response*/) throws IOException {
        var jwtResponse = authService.exchangeOneTimeCode(oneTimeCodeRequest);

//        Cookie cookie = new Cookie("ACCESS_TOKEN", jwtResponse.getAccessToken());
//        cookie.setHttpOnly(true);
//        cookie.setSecure(false);
//        cookie.setPath("/");
//        cookie.setMaxAge(15 * 60);
//
//        response.addCookie(cookie);

        ResponseCookie cookie = ResponseCookie.from("STREAM_TOKEN", jwtResponse.getStreamingToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMinutes(cookieTtlMinutes))
                .sameSite("Lax")
                .build();

        authService.addStreamingCookieToStore(jwtResponse.getStreamingToken(), cookie);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(JwtResponseDTO.builder()
                .accessToken(jwtResponse.getAccessToken())
                .refreshToken(jwtResponse.getRefreshToken())
                .build());
    }

    @PostMapping("/refresh-access")
    public ResponseEntity<JwtResponseDTO> refreshAccess(@RequestBody JwtRefreshRequest refreshRequest) {
        var jwtResponse = authService.getAccessToken(refreshRequest.getRefreshToken());

        ResponseCookie cookie = ResponseCookie.from("STREAM_TOKEN", jwtResponse.getStreamingToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMinutes(cookieTtlMinutes))
                .sameSite("Lax")
                .build();

        authService.addStreamingCookieToStore(jwtResponse.getStreamingToken(), cookie);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(JwtResponseDTO.builder()
                .accessToken(jwtResponse.getAccessToken())
                .refreshToken(jwtResponse.getRefreshToken())
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDTO> refresh(@RequestBody JwtRefreshRequest refreshRequest) {
        var jwtResponse = authService.refreshTokens(refreshRequest.getRefreshToken());

        ResponseCookie cookie = ResponseCookie.from("STREAM_TOKEN", jwtResponse.getStreamingToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMinutes(cookieTtlMinutes))
                .sameSite("Lax")
                .build();

        authService.addStreamingCookieToStore(jwtResponse.getStreamingToken(), cookie);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(JwtResponseDTO.builder()
                .accessToken(jwtResponse.getAccessToken())
                .refreshToken(jwtResponse.getRefreshToken())
                .build());
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> resetPasswordRequest(final @RequestBody RequestPasswordResetRequest passwordResetRequest) throws MessagingException, FileNotFoundException {
        authService.requestPasswordReset(passwordResetRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Void> resetPassword(final @RequestBody PasswordResetRequest passwordResetRequest) throws ExecutionControl.NotImplementedException, MessagingException, FileNotFoundException {
        authService.resetPassword(passwordResetRequest);
        return ResponseEntity.ok().build();
    }
}
