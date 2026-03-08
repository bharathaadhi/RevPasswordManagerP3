package com.rev.generatorservice.controller;

import com.rev.generatorservice.dto.GeneratorRequestDto;
import com.rev.generatorservice.dto.GeneratorResponseDto;
import com.rev.generatorservice.dto.VaultRequestDto;
import com.rev.generatorservice.service.GeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/generator")
@RequiredArgsConstructor
public class GeneratorController {

    private final GeneratorService generatorService;

    @PostMapping("/generate")
    public GeneratorResponseDto generate(@RequestBody GeneratorRequestDto dto) {
        return generatorService.generatePassword(dto);
    }
    @PostMapping("/save-to-vault")
    public String saveToVault(@RequestBody VaultRequestDto vaultDto) {

        GeneratorRequestDto dto = new GeneratorRequestDto();
        dto.setLength(12);
        dto.setUppercase(true);
        dto.setLowercase(true);
        dto.setNumbers(true);
        dto.setSymbols(true);
        dto.setExcludeSimilar(true);

        return generatorService.generateAndSave(dto, vaultDto);
    }
    @PostMapping("/generate-multiple")
    public List<String> generateMultiple(@RequestBody GeneratorRequestDto dto) {
        return generatorService.generateMultiplePasswords(dto);
    }

}