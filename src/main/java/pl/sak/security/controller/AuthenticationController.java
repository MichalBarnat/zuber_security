package pl.sak.security.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sak.security.model.request.AuthenticationRequest;
import pl.sak.security.model.request.ChangePasswordRequest;
import pl.sak.security.model.request.RegisterRequest;
import pl.sak.security.model.response.AuthenticationResponse;
import pl.sak.security.model.response.ChangePasswordResponse;
import pl.sak.security.service.AuthenticationService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/auths")
@RequiredArgsConstructor
@Tag(name = "Authorization", description = "Authorization api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "User registration.",
            description = "This operation enables both users and administrators to registers the user"
                    + " using RegisterRequest class in the body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid RegisterRequest request) {
        AuthenticationResponse response = authenticationService.register(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "User authenticate.",
            description = "This operation enables both users and administrators to authenticate to the user"
                    + " using AuthenticationRequest class in the body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request,
                                                                 @RequestHeader(value = AUTHORIZATION) String authorization) {
        ChangePasswordResponse response = authenticationService.changePassword(request, authorization);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
