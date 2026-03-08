package com.rev.generatorservice.service;

import com.rev.generatorservice.dto.GeneratorRequestDto;
import com.rev.generatorservice.dto.GeneratorResponseDto;
import com.rev.generatorservice.dto.VaultRequestDto;

import java.util.List;

public interface GeneratorService {

    GeneratorResponseDto generatePassword(GeneratorRequestDto dto);

    String generateAndSave(GeneratorRequestDto dto, VaultRequestDto vaultDto);

    List<String> generateMultiplePasswords(GeneratorRequestDto dto);
}