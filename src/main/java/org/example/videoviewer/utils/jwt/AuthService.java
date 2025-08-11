package org.example.videoviewer.utils.jwt;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.videoviewer.enums.Roles;
import org.example.videoviewer.exceptions.AuthenticationException;
import org.example.videoviewer.repositories.model.Users;
import org.example.videoviewer.services.UsersService;
import org.example.videoviewer.utils.jwt.dto.JwtRequest;
import org.example.videoviewer.utils.jwt.dto.JwtResponse;
import org.example.videoviewer.utils.jwt.dto.OneTimeCodeRequest;
import org.example.videoviewer.utils.jwt.dto.RegistrationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

//todo: Implement One Time Token flow
@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtProvider jwtProvider;
    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;

    private static final HashFunction hasher = Hashing.sha256();
    private static final Random random = new Random();

    private final Map<String, String> jwtRefreshTokenStore = new HashMap<>();

    public JwtResponse login(final @NonNull JwtRequest request) {
        try {
            var user = usersService.getByUsername(request.getLogin()).orElseThrow(AuthenticationException::new);
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                var oneTimeCode = generateOneTimeCode(user);

                //todo: remove
                return JwtResponse.builder()
                        .accessToken(oneTimeCode)
                        .build();

//                return JwtResponse.builder()
//                        .accessToken(accessToken)
//                        .refreshToken(refreshToken)
//                        .build();
            } else {
                throw new AuthenticationException();
            }
        } catch (AuthenticationException e) {
            throw new AuthenticationException();
        }
    }

    private String generateOneTimeCode(final Users user) {
        var oneTimeCode = getRandomNumber();
        user.setOneTimeToken(getSha256Hash(oneTimeCode));
        System.out.println(user.getOneTimeToken());
        usersService.save(user);
        return oneTimeCode;
    }

    private String getRandomNumber() {
        var min = 10000;
        var max = 99999;
        return Integer.toString(random.ints(min, max).findFirst().orElse(min));
    }

    private String getSha256Hash(final String origin) {
        return hasher.hashString(origin, StandardCharsets.UTF_8).toString();
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

            var oneTimeCode = generateOneTimeCode(newUser);

            //todo: remove
            return JwtResponse.builder()
                    .accessToken(oneTimeCode)
                    .build();

//            usersService.save(newUser);
//            return login(JwtRequest.builder()
//                    .login(newUser.getUsername())
//                    .password(request.getPassword())
//                    .build());
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

    private JwtResponse getTokens(final Users user) {
        var accessToken = jwtProvider.generateAccessToken(user);
        var refreshToken = jwtProvider.generateRefreshToken(user);
        jwtRefreshTokenStore.put(user.getUsername(), refreshToken);

        return JwtResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    private void removeOneTimeToken(final Users user) {
        user.setOneTimeToken(null);
        usersService.save(user);
    }

    public JwtResponse exchangeOneTimeCode(@NonNull final OneTimeCodeRequest request) {
        var user = usersService.getByUsername(request.getUsername()).orElseThrow(AuthenticationException::new);
        var oneTimeToken = user.getOneTimeToken();
        removeOneTimeToken(user);

        if (getSha256Hash(request.getCode()).equals(oneTimeToken)) {
            return getTokens(user);
        }

        throw new AuthenticationException("You provided wrong one-time code");
    }
}
