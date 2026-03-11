package com.rev.userservice.service;

import com.rev.userservice.dto.LoginRequest;
import com.rev.userservice.dto.RegisterRequest;
import com.rev.userservice.model.User;
import com.rev.userservice.repository.UserRepository;
import com.rev.userservice.security.JwtUtil;
import com.rev.userservice.client.NotificationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@ex.com");
        testUser.setName("Tester");
        testUser.setPassword("encoded_pass");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@ex.com");
        registerRequest.setMasterPassword("raw_pass");
        registerRequest.setName("Tester");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@ex.com");
        loginRequest.setMasterPassword("raw_pass");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail("test@ex.com")).thenReturn(false);
        when(passwordEncoder.encode("raw_pass")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.register(registerRequest);

        assertNotNull(savedUser);
        assertEquals("test@ex.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ThrowsExceptionWhenEmailExists() {
        when(userRepository.existsByEmail("test@ex.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findFirstByEmail("test@ex.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("raw_pass", "encoded_pass")).thenReturn(true);
        when(jwtUtil.generateToken("test@ex.com")).thenReturn("mocked_jwt_token");

        String token = userService.login(loginRequest);

        assertEquals("mocked_jwt_token", token);
        // Verify notification was invoked
        verify(notificationClient, times(1)).sendNotification(anyMap());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        when(userRepository.findFirstByEmail("test@ex.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong_pass", "encoded_pass")).thenReturn(false);
        
        LoginRequest invalidLogin = new LoginRequest();
        invalidLogin.setEmail("test@ex.com");
        invalidLogin.setMasterPassword("wrong_pass");

        assertThrows(RuntimeException.class, () -> userService.login(invalidLogin));
    }
}
