package pl.sak.security.model.request;

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
public class ChangePasswordRequest {

    @NotBlank(message = "CURRENT_PASSWORD_NOT_BLANK")
    private String currentPassword;
    @NotBlank(message = "NEW_PASSWORD_NOT_BLANK")
    private String newPassword;
    @NotBlank(message = "CONFIRMATION_PASSWORD_NOT_BLANK")
    private String confirmationPassword;
}
