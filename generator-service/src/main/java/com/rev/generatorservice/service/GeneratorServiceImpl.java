package com.rev.generatorservice.service;

import com.rev.generatorservice.dto.GeneratorRequestDto;
import com.rev.generatorservice.dto.GeneratorResponseDto;
import com.rev.generatorservice.dto.VaultRequestDto;
import com.rev.generatorservice.feign.VaultFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {

    private final VaultFeignClient vaultFeignClient;

    @Override
    public GeneratorResponseDto generatePassword(GeneratorRequestDto dto) {
        if (dto.getLength() < 8 || dto.getLength() > 64) {
            throw new RuntimeException("Password length must be between 8 and 64");
        }

        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String nums = "0123456789";
        String symbols = "@#$%&*!";

        if (dto.isExcludeSimilar()) {
            upper = upper.replace("O", "").replace("I", "");
            lower = lower.replace("l", "");
            nums = nums.replace("0", "").replace("1", "");
        }

        String characters = "";

        if (dto.isUpper()) characters += upper;
        if (dto.isLower()) characters += lower;
        if (dto.isNumber()) characters += nums;
        if (dto.isSpecial()) characters += symbols;

        if (characters.isEmpty()) {
            throw new RuntimeException("Select at least one character type");
        }

        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < dto.getLength(); i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }

        String strength = calculateStrength(password.toString());

        return new GeneratorResponseDto(password.toString(), strength);
    }

    @Override
    public String generateAndSave(GeneratorRequestDto dto, VaultRequestDto vaultDto) {

        GeneratorResponseDto generated = generatePassword(dto);

        vaultDto.setEncryptedPassword(generated.getPassword());
        vaultDto.setStrength(generated.getStrength());

        vaultFeignClient.saveToVault(vaultDto);

        return "Generated password saved to vault successfully";
    }

    private String calculateStrength(String password) {

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[@#$%&*!].*")) score++;

        if (score <= 2) return "Weak";
        if (score == 3) return "Medium";
        if (score == 4) return "Strong";

        return "Very Strong";
    }
    @Override
    public List<String> generateMultiplePasswords(GeneratorRequestDto dto) {

        List<String> passwords = new ArrayList<>();

        int iterations = dto.getCount() > 0 ? dto.getCount() : 5;

        for (int i = 0; i < iterations; i++) {
            passwords.add(generatePassword(dto).getPassword());
        }

        return passwords;
    }
}