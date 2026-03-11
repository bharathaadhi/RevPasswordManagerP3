package com.rev.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAuditServiceTest {

    private SecurityAuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new SecurityAuditService();
    }

    @Test
    void countWeakPasswords_ReturnsCorrectCount() {
        List<String> passwords = List.of("weak", "strongpassword", "123", "good1234");
        // "weak" (<6) and "123" (<6)
        int weakCount = auditService.countWeakPasswords(passwords);
        assertEquals(2, weakCount);
    }

    @Test
    void countReusedPasswords_ReturnsCorrectCount() {
        List<String> passwords = List.of("pass1", "pass2", "pass1", "pass3", "pass2");
        int reusedCount = auditService.countReusedPasswords(passwords);
        assertEquals(2, reusedCount);
    }

    @Test
    void findWeakPasswords_IdentifiesWeakCriteria() {
        // Less than 8, or no uppercase, or no number
        List<String> passwords = List.of(
                "Short1!",      // length < 8 -> weak
                "nouppercase1",// no uppercase -> weak
                "NoNumberHere",// no number -> weak
                "StrongPass1"  // strong
        );

        List<String> weakList = auditService.findWeakPasswords(passwords);
        assertEquals(3, weakList.size());
        assertTrue(weakList.contains("Short1!"));
        assertFalse(weakList.contains("StrongPass1"));
    }
}
