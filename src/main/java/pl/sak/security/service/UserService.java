package pl.sak.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pl.sak.security.exception.UserEmailNotFoundException;
import pl.sak.security.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UserEmailNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserEmailNotFoundException(email));
    }

    public boolean extractUsername(String email) {
        return userRepository.existsByEmail(email);
    }
}
