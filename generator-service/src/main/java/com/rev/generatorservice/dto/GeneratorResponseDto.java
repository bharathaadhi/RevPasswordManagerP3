package com.rev.generatorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneratorResponseDto {
    private String password;
    private String strength;
}