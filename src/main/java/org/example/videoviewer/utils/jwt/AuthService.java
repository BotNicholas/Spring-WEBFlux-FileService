package org.example.videoviewer.utils.jwt;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.enums.Roles;
import org.example.videoviewer.exceptions.AuthenticationException;
import org.example.videoviewer.repositories.model.Users;
import org.example.videoviewer.services.UsersService;
import org.example.videoviewer.utils.jwt.dto.JwtRequest;
import org.example.videoviewer.utils.jwt.dto.JwtResponse;
import org.example.videoviewer.utils.jwt.dto.RegistrationRequest;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//todo: Implement One Time Token flow
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtProvider jwtProvider;
    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, String> jwtRefreshTokenStore = new HashMap<>();

    public JwtResponse login(final @NonNull JwtRequest request) {
        try {
            var user = usersService.getByUsername(request.getLogin()).orElseThrow(AuthenticationException::new);
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                var accessToken = jwtProvider.generateAccessToken(user);
                var refreshToken = jwtProvider.generateRefreshToken(user);
                jwtRefreshTokenStore.put(user.getUsername(), refreshToken);
                return JwtResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
            } else {
                throw new AuthenticationException();
            }
        } catch (AuthenticationException e) {
            throw new AuthenticationException();
        }
    }

    public JwtResponse register(@NonNull final RegistrationRequest request) {
        var user = usersService.getByUsername(request.getUsername());
        if (user.isEmpty()) {
            var newUser = Users.builder()
                    .name(request.getName())
                    .surname(request.getSurname())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .roles(Set.of(Roles.USER))
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();
            usersService.save(newUser);
            return login(JwtRequest.builder()
                    .login(newUser.getUsername())
                    .password(request.getPassword())
                    .build());
        } else {
            throw new AuthenticationException("Select another username");
        }
    }

    public JwtResponse getAccessToken(@NonNull final String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            var claims = jwtProvider.getRefreshClaims(refreshToken);
            var username = claims.getSubject();

            var storedRefreshToken = jwtRefreshTokenStore.get(username);
            if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
                var user = usersService.getByUsername(username).orElseThrow(AuthenticationException::new);
                var accessToken = jwtProvider.generateAccessToken(user);

                return JwtResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(null)
                        .build();
            }
        }
        return JwtResponse.builder()
                .accessToken(null)
                .refreshToken(null)
                .build();
    }

    public JwtResponse refreshTokens(@NonNull final String refreshToken) {
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            var claims = jwtProvider.getRefreshClaims(refreshToken);
            var username = claims.getSubject();

            var storedRefreshToken = jwtRefreshTokenStore.get(username);
            if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)) {
                var user = usersService.getByUsername(username).orElseThrow(AuthenticationException::new);
                var accessToken = jwtProvider.generateAccessToken(user);
                var newRefreshToken = jwtProvider.generateRefreshToken(user);
                jwtRefreshTokenStore.put(username, newRefreshToken);

                return JwtResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .build();
            }
        }
        throw new AuthenticationException("Invalid RefreshToken");
    }
}
