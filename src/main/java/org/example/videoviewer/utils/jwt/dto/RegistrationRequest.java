package org.example.videoviewer.utils.jwt.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    private String name;
    private String surname;
    private String username;
    @Pattern(regexp = ".*@.*\\..*", message = "Email must be in the following format 'example@mail.com'")
    private String email;
    private String password;
}
