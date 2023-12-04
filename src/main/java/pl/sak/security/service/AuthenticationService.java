package pl.sak.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sak.security.exception.InvalidPasswordException;
import pl.sak.security.exception.UserEmailNotFoundException;
import pl.sak.security.model.User;
import pl.sak.security.model.request.AuthenticationRequest;
import pl.sak.security.model.request.ChangePasswordRequest;
import pl.sak.security.model.request.RegisterRequest;
import pl.sak.security.model.response.AuthenticationResponse;
import pl.sak.security.model.response.ChangePasswordResponse;
import pl.sak.security.repository.UserRepository;

import static pl.sak.security.enums.UserRole.USER;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(USER)
                .build();
        userRepository.save(user);
        var jwtToken = jwtTokenService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserEmailNotFoundException(request.getEmail()));
        var jwtToken = jwtTokenService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional
    public ChangePasswordResponse changePassword(ChangePasswordRequest request, String authorization) {
        String credentials = authorization.substring("Bearer ".length()).trim();
        String email = jwtTokenService.extractUsername(credentials);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserEmailNotFoundException(email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Wrong password!");
        }

        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new InvalidPasswordException("Password are not the same!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ChangePasswordResponse.builder()
                .message("Password changed successfully.")
                .build();
    }
}
