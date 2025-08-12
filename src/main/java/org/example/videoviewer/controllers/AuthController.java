package org.example.videoviewer.controllers;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.security.jwt.AuthService;
import org.example.videoviewer.security.jwt.dto.JwtRefreshRequest;
import org.example.videoviewer.security.jwt.dto.JwtRequest;
import org.example.videoviewer.security.jwt.dto.JwtResponse;
import org.example.videoviewer.security.jwt.dto.OneTimeCodeRequest;
import org.example.videoviewer.security.jwt.dto.RegistrationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) throws MessagingException, FileNotFoundException {
        authService.register(registrationRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest loginRequest) throws MessagingException, FileNotFoundException {
        authService.login(loginRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/exchange-one-time-code")
    public ResponseEntity<JwtResponse> exchangeOneTimeCode(@RequestBody OneTimeCodeRequest oneTimeCodeRequest) throws IOException {
        return ResponseEntity.ok(authService.exchangeOneTimeCode(oneTimeCodeRequest));
    }

    @PostMapping("/refresh-access")
    public ResponseEntity<JwtResponse> refreshAccess(@RequestBody JwtRefreshRequest refreshRequest) {
        return ResponseEntity.ok(authService.getAccessToken(refreshRequest.getRefreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody JwtRefreshRequest refreshRequest) {
        return ResponseEntity.ok(authService.refreshTokens(refreshRequest.getRefreshToken()));
    }
}
