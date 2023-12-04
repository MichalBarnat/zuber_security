package pl.sak.security.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuthenticationRequest {

    @Email(message = "INCORRECT_EMAIL_FORMAT")
    @NotBlank(message = "EMAIL_NOT_BLANK")
    private String email;
    @NotBlank(message = "PASSWORD_NOT_BLANK")
    private String password;
}
