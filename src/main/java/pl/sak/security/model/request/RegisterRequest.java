package pl.sak.security.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.sak.security.validates.UniqueEmail;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "NAME_NOT_BLANK")
    private String name;
    @NotBlank(message = "SURNAME_NOT_BLANK")
    private String surname;
    @UniqueEmail(message = "GIVEN_EMAIL_EXISTS")
    @Email(message = "INCORRECT_EMAIL_FORMAT")
    @NotBlank(message = "EMAIL_NOT_BLANK")
    private String email;
    @NotBlank(message = "PASSWORD_NOT_BLANK")
    private String password;
}
