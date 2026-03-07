package com.rev.security.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class VerificationCodeService {

    public String generateCode() {

        Random random = new Random();

        int code = 100000 + random.nextInt(900000);

        return String.valueOf(code);
    }
}