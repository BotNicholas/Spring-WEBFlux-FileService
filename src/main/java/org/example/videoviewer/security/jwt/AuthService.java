package org.example.videoviewer.security.jwt;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import jakarta.mail.MessagingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.videoviewer.enums.Roles;
import org.example.videoviewer.exceptions.AuthenticationException;
import org.example.videoviewer.exceptions.FileExistsException;
import org.example.videoviewer.mail.Mailer;
import org.example.videoviewer.repositories.model.Users;
import org.example.videoviewer.services.FilesService;
import org.example.videoviewer.services.UsersService;
import org.example.videoviewer.security.jwt.dto.JwtRequest;
import org.example.videoviewer.security.jwt.dto.JwtResponse;
import org.example.videoviewer.security.jwt.dto.OneTimeCodeRequest;
import org.example.videoviewer.security.jwt.dto.RegistrationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.example.videoviewer.mail.Templates.ONE_TIME_CODE_MESSAGE_TEMPLATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtProvider jwtProvider;
    private final UsersService usersService;
    private final PasswordEncoder passwordEncoder;
    private final Mailer mailer;
    private final FilesService filesService;

    private static final HashFunction hasher = Hashing.sha256();
    private static final Random random = new Random();

    private final Map<String, String> jwtRefreshTokenStore = new HashMap<>();

    public void login(final @NonNull JwtRequest request) throws MessagingException, FileNotFoundException {
        var user = usersService.getByUsername(request.getLogin()).orElseThrow(AuthenticationException::new);
        if (!user.getVerified()) {
            throw new AuthenticationException("User is not verified");
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            var oneTimeCode = generateOneTimeCode(user);

            mailer.sendHtmlMail(user.getEmail(), "One Time Code", MessageFormat.format(ONE_TIME_CODE_MESSAGE_TEMPLATE, (user.getName() + " " + user.getSurname()), "Login", oneTimeCode));
        } else {
            throw new AuthenticationException();
        }
    }

    public void register(@NonNull final RegistrationRequest request) throws MessagingException, FileNotFoundException {
        var user = usersService.getByUsername(request.getUsername());
        if (user.isEmpty()) {
            var newUser = Users.builder()
                    .name(request.getName())
                    .surname(request.getSurname())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .roles(Set.of(Roles.USER))
                    .password(passwordEncoder.encode(request.getPassword()))
                    .verified(false)
                    .build();

            var oneTimeCode = generateOneTimeCode(newUser);

            mailer.sendHtmlMail(newUser.getEmail(), "One Time Code", MessageFormat.format(ONE_TIME_CODE_MESSAGE_TEMPLATE, (newUser.getName() + " " + newUser.getSurname()), "Registration", oneTimeCode));
        } else {
            throw new AuthenticationException("Select another username");
        }
    }


    private String generateOneTimeCode(final Users user) {
        var oneTimeCode = getRandomNumber();
        user.setOneTimeToken(getSha256Hash(oneTimeCode));
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

    private void createUserFolder(final Users user) throws IOException {
        if (!user.getRoles().contains(Roles.ADMIN) && !user.getVerified()) {
            try {
                filesService.createDirectoryAtForUser("/", "", user);
            } catch (FileExistsException e) {
                log.warn("Skipping folder creation for user {} - folder already exists", user.getUsername());
            }
        }
    }

    public JwtResponse exchangeOneTimeCode(@NonNull final OneTimeCodeRequest request) throws IOException {
        var user = usersService.getByUsername(request.getUsername()).orElseThrow(AuthenticationException::new);
        var oneTimeToken = user.getOneTimeToken();
        user.setOneTimeToken(null);

        if (getSha256Hash(request.getCode()).equals(oneTimeToken)) {
            createUserFolder(user);
            user.setVerified(true);
            usersService.save(user);
            return getTokens(user);
        }

        usersService.save(user);

        throw new AuthenticationException("You provided wrong one-time code");
    }
}
