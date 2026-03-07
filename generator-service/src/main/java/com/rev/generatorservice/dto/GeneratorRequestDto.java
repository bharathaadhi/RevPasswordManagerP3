package com.rev.generatorservice.dto;

import lombok.Data;

@Data
public class GeneratorRequestDto {
    private boolean excludeSimilar;
    private int length;
    private boolean uppercase;
    private boolean lowercase;
    private boolean numbers;
    private boolean symbols;
}