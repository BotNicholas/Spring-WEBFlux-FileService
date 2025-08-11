package org.example.videoviewer.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.utils.jwt.AuthService;
import org.example.videoviewer.utils.jwt.dto.JwtRefreshRequest;
import org.example.videoviewer.utils.jwt.dto.JwtRequest;
import org.example.videoviewer.utils.jwt.dto.JwtResponse;
import org.example.videoviewer.utils.jwt.dto.RegistrationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
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
