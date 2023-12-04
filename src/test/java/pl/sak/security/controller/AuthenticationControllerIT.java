package pl.sak.security.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import liquibase.exception.LiquibaseException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.sak.security.DatabaseCleaner;
import pl.sak.security.SecurityApplication;
import pl.sak.security.exception.dto.ValidationErrorDto;
import pl.sak.security.model.request.AuthenticationRequest;
import pl.sak.security.model.request.ChangePasswordRequest;
import pl.sak.security.model.request.RegisterRequest;
import pl.sak.security.model.response.AuthenticationResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SecurityApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerIT {

    private final MockMvc postman;
    private final ObjectMapper objectMapper;
    private final DatabaseCleaner databaseCleaner;

    @Autowired
    public AuthenticationControllerIT(MockMvc postman, ObjectMapper objectMapper, DatabaseCleaner databaseCleaner) {
        this.postman = postman;
        this.objectMapper = objectMapper;
        this.databaseCleaner = databaseCleaner;
    }

    @AfterEach
    void tearDown() throws LiquibaseException {
        databaseCleaner.cleanUp();
    }

    @Test
    void shouldRegisterUser() throws Exception {
        //Given
        RegisterRequest request = RegisterRequest.builder()
                .name("test")
                .surname("test")
                .email("test@example.com")
                .password("test")
                .build();

        String json = objectMapper.writeValueAsString(request);

        //When
        MvcResult result = postman.perform(post("/auths/register")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonContent = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonContent, AuthenticationResponse.class);

        //Then
        assertNotNull(response.getToken());
    }

    @Test
    void shouldAuthenticateUser() throws Exception {
        //Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("b.bartek@example.com")
                .password("admin")
                .build();

        String json = objectMapper.writeValueAsString(request);

        //When
        MvcResult result = postman.perform(post("/auths/authenticate")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonContent = result.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonContent, AuthenticationResponse.class);

        //Then
        assertNotNull(response.getToken());
    }

    @Test
    void shouldChangePasswordForUser() throws Exception {
        //Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("b.bartek@example.com")
                .password("admin")
                .build();

        String json = objectMapper.writeValueAsString(request);

        MvcResult authenticate = postman.perform(post("/auths/authenticate")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String jsonContent = authenticate.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonContent, AuthenticationResponse.class);

        String token = "Bearer " + response.getToken();

        //When
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("admin")
                .newPassword("test")
                .confirmationPassword("test")
                .build();

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordRequest);

        //Then
        postman.perform(patch("/auths/change-password")
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully."));
    }

    @Test
    void shouldNotChangePasswordForUserWhenCurrentPasswordIsBlank() throws Exception {
        //Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("b.bartek@example.com")
                .password("admin")
                .build();

        String json = objectMapper.writeValueAsString(request);

        MvcResult authenticate = postman.perform(post("/auths/authenticate")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String jsonContent = authenticate.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonContent, AuthenticationResponse.class);

        String token = "Bearer " + response.getToken();

        //When
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("")
                .newPassword("test")
                .confirmationPassword("test")
                .build();

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordRequest);

        //Then
        String responseJson = postman.perform(patch("/auths/change-password")
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'currentPassword' && @.code == 'CURRENT_PASSWORD_NOT_BLANK')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldChangePasswordForUserWhenNewPasswordIsBlank() throws Exception {
        //Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("b.bartek@example.com")
                .password("admin")
                .build();

        String json = objectMapper.writeValueAsString(request);

        MvcResult authenticate = postman.perform(post("/auths/authenticate")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String jsonContent = authenticate.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonContent, AuthenticationResponse.class);

        String token = "Bearer " + response.getToken();

        //When
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("admin")
                .newPassword("")
                .confirmationPassword("test")
                .build();

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordRequest);

        //Then
        String responseJson = postman.perform(patch("/auths/change-password")
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'newPassword' && @.code == 'NEW_PASSWORD_NOT_BLANK')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotChangePasswordForUserWhenConfirmationPasswordIsBlank() throws Exception {
        //Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("b.bartek@example.com")
                .password("admin")
                .build();

        String json = objectMapper.writeValueAsString(request);

        MvcResult authenticate = postman.perform(post("/auths/authenticate")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String jsonContent = authenticate.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonContent, AuthenticationResponse.class);

        String token = "Bearer " + response.getToken();

        //When
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("admin")
                .newPassword("test")
                .confirmationPassword("")
                .build();

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordRequest);

        //Then
        String responseJson = postman.perform(patch("/auths/change-password")
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.field == 'confirmationPassword' && @.code == 'CONFIRMATION_PASSWORD_NOT_BLANK')]").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ValidationErrorDto> errors = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(1, errors.size());
    }

    @Test
    void shouldNotChangePasswordForUserWithInvalidCurrentPassword() throws Exception {
        //Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("b.bartek@example.com")
                .password("admin")
                .build();

        String json = objectMapper.writeValueAsString(request);

        MvcResult authenticate = postman.perform(post("/auths/authenticate")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String jsonContent = authenticate.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonContent, AuthenticationResponse.class);

        String token = "Bearer " + response.getToken();

        //When
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("user")
                .newPassword("test")
                .confirmationPassword("test")
                .build();

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordRequest);

        //Then
        postman.perform(patch("/auths/change-password")
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Wrong password!"))
                .andExpect(jsonPath("$.uri").value("/auths/change-password"))
                .andExpect(jsonPath("$.method").value("PATCH"));
    }

    @Test
    void shouldNotChangePasswordForUserWithInvalidNewPasswordAndConfirmationPassword() throws Exception {
        //Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("b.bartek@example.com")
                .password("admin")
                .build();

        String json = objectMapper.writeValueAsString(request);

        MvcResult authenticate = postman.perform(post("/auths/authenticate")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String jsonContent = authenticate.getResponse().getContentAsString();
        AuthenticationResponse response = objectMapper.readValue(jsonContent, AuthenticationResponse.class);

        String token = "Bearer " + response.getToken();

        //When
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("admin")
                .newPassword("test")
                .confirmationPassword("TEST")
                .build();

        String jsonChangePassword = objectMapper.writeValueAsString(changePasswordRequest);

        //Then
        postman.perform(patch("/auths/change-password")
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonChangePassword))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Password are not the same!"))
                .andExpect(jsonPath("$.uri").value("/auths/change-password"))
                .andExpect(jsonPath("$.method").value("PATCH"));
    }
}