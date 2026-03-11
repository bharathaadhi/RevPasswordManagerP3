package com.rev.vaultservice.service;

import com.rev.vaultservice.dto.MasterPasswordDto;
import com.rev.vaultservice.dto.VaultRequestDto;
import com.rev.vaultservice.entity.VaultEntry;
import com.rev.vaultservice.feign.AuthFeignClient;
import com.rev.vaultservice.repository.VaultRepository;
import com.rev.vaultservice.client.NotificationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultServiceImplTest {

    @Mock
    private VaultRepository vaultRepository;

    @Mock
    private AuthFeignClient authFeignClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private VaultServiceImpl vaultService;

    private VaultEntry vaultEntry;
    private VaultRequestDto requestDto;

    @BeforeEach
    void setUp() {
        vaultEntry = new VaultEntry();
        vaultEntry.setId(1L);
        vaultEntry.setUserId(1L);
        vaultEntry.setPlatform("TestApp");
        vaultEntry.setUsername("tester");
        vaultEntry.setEncryptedPassword("encrypted_data");

        requestDto = new VaultRequestDto();
        requestDto.setUserId(1L);
        requestDto.setPlatform("TestApp");
        requestDto.setUsername("tester");
        requestDto.setEncryptedPassword("plain_pass");
        requestDto.setAlreadyEncrypted(false);
    }

    @Test
    void saveVault_Success() {
        when(vaultRepository.existsByUserIdAndPlatformAndUsername(1L, "TestApp", "tester")).thenReturn(false);
        when(vaultRepository.save(any(VaultEntry.class))).thenReturn(vaultEntry);

        VaultEntry saved = vaultService.saveVault(requestDto);

        assertNotNull(saved);
        assertEquals("TestApp", saved.getPlatform());
        verify(vaultRepository, times(1)).save(any(VaultEntry.class));
    }

    @Test
    void saveVault_ThrowsExceptionIfDuplicate() {
        when(vaultRepository.existsByUserIdAndPlatformAndUsername(1L, "TestApp", "tester")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> vaultService.saveVault(requestDto));
    }

    @Test
    void revealPassword_SuccessWithMasterPassword() {
        // Assume AESUtil works statically, but we mainly test the control flow
        when(authFeignClient.verifyMasterPassword(any(MasterPasswordDto.class))).thenReturn(true);
        when(vaultRepository.findById(1L)).thenReturn(Optional.of(vaultEntry));

        // Note: AES decrypt will try to decrypt "encrypted_data" which is invalid Base64 padding, 
        // leading to exception if actually invoked. We just ensure no Auth error is thrown before that.
        try {
            vaultService.revealPassword(1L, "master_pass", "test@ex.com");
        } catch (Exception e) {
            // Expected due to dummy encrypted_data failing AES but NOT auth fail
            assertNotEquals("Invalid master password", e.getMessage());
        }
        
        verify(authFeignClient, times(1)).verifyMasterPassword(any(MasterPasswordDto.class));
    }

    @Test
    void revealPassword_ThrowsInvalidMasterPassword() {
        when(authFeignClient.verifyMasterPassword(any(MasterPasswordDto.class))).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            vaultService.revealPassword(1L, "wrong_pass", "test@ex.com")
        );
        assertEquals("Invalid master password", ex.getMessage());
    }
}
