package pl.sak.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    @InjectMocks
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtTokenService = new JwtTokenService();
    }

    @Test
    void shouldExtractUsername() {
        //Given

    }

    @Test
    void extractClaim() {
    }

    @Test
    void generateToken() {
    }

    @Test
    void testGenerateToken() {
    }

    @Test
    void isTokenValid() {
    }
}