package com.rev.generatorservice.service;

import com.rev.generatorservice.dto.GeneratorRequestDto;
import com.rev.generatorservice.dto.GeneratorResponseDto;
import com.rev.generatorservice.dto.VaultRequestDto;
import com.rev.generatorservice.feign.VaultFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneratorServiceImplTest {

    @Mock
    private VaultFeignClient vaultFeignClient;

    @InjectMocks
    private GeneratorServiceImpl generatorService;

    private GeneratorRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new GeneratorRequestDto();
        requestDto.setLength(12);
        requestDto.setUpper(true);
        requestDto.setLower(true);
        requestDto.setNumber(true);
        requestDto.setSpecial(true);
        requestDto.setExcludeSimilar(false);
    }

    @Test
    void generatePassword_Success() {
        GeneratorResponseDto response = generatorService.generatePassword(requestDto);

        assertNotNull(response);
        assertNotNull(response.getPassword());
        assertEquals(12, response.getPassword().length());
        assertNotNull(response.getStrength());
    }

    @Test
    void generatePassword_ThrowsExceptionIfLengthInvalid() {
        requestDto.setLength(5); // Minimum is 8

        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            generatorService.generatePassword(requestDto)
        );
        assertEquals("Password length must be between 8 and 64", ex.getMessage());
    }

    @Test
    void generatePassword_ThrowsExceptionIfNoCharactersSelected() {
        requestDto.setUpper(false);
        requestDto.setLower(false);
        requestDto.setNumber(false);
        requestDto.setSpecial(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> 
            generatorService.generatePassword(requestDto)
        );
        assertEquals("Select at least one character type", ex.getMessage());
    }

    @Test
    void generateAndSave_Success() {
        VaultRequestDto vaultDto = new VaultRequestDto();
        when(vaultFeignClient.saveToVault(any(VaultRequestDto.class))).thenReturn(null);

        String result = generatorService.generateAndSave(requestDto, vaultDto);

        assertEquals("Generated password saved to vault successfully", result);
        verify(vaultFeignClient, times(1)).saveToVault(any(VaultRequestDto.class));
    }

    @Test
    void generateMultiplePasswords_Success() {
        requestDto.setCount(3);
        List<String> passwords = generatorService.generateMultiplePasswords(requestDto);

        assertNotNull(passwords);
        assertEquals(3, passwords.size());
    }
}
