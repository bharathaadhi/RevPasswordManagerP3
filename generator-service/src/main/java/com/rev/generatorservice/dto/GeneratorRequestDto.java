package com.rev.generatorservice.dto;

import lombok.Data;

@Data
public class GeneratorRequestDto {
    private boolean excludeSimilar;
    private int length;
    private int count;
    private boolean upper;
    private boolean lower;
    private boolean number;
    private boolean special;
}