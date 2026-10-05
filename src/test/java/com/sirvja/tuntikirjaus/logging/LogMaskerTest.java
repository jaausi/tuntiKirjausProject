package com.sirvja.tuntikirjaus.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogMaskerTest {

    private final String originalHome = System.getProperty("user.home");

    @BeforeEach
    void setUp() {
        LogMasker.setUserHome("/Users/matti.meikalainen");
    }

    @AfterEach
    void tearDown() {
        LogMasker.setUserHome(originalHome);
    }

    @Test
    void masksHomeDirectory() {
        assertEquals("Using database in location: ~/tuntikirjaus/database/tuntikirjaus.db",
                LogMasker.mask("Using database in location: /Users/matti.meikalainen/tuntikirjaus/database/tuntikirjaus.db"));
    }

    @Test
    void masksEmailAddresses() {
        String masked = LogMasker.mask("Logged in as matti.meikalainen@example.com");
        assertFalse(masked.contains("matti"));
        assertTrue(masked.contains("****@****"));
    }

    @Test
    void masksCredentialsInUrl() {
        assertEquals("Connecting to https://****@server.example/path",
                LogMasker.mask("Connecting to https://user:secretpass@server.example/path"));
    }

    @Test
    void masksSecretValues() {
        String masked = LogMasker.mask("password=hunter2 token: abc123 \"apiKey\":\"xyz\" Authorization: Bearer eyJhbGciOi");
        assertFalse(masked.contains("hunter2"));
        assertFalse(masked.contains("abc123"));
        assertFalse(masked.contains("xyz"));
        assertFalse(masked.contains("eyJhbGciOi"));
    }

    @Test
    void keepsOrdinaryMessagesUnchanged() {
        String message = "Inserting Tuntikirjaus: TuntiKirjaus{id=5, startTime=2026-10-05T08:00, endTime=-, remote=false}";
        assertEquals(message, LogMasker.mask(message));
    }

    @Test
    void handlesNullAndEmpty() {
        assertNull(LogMasker.mask(null));
        assertEquals("", LogMasker.mask(""));
    }
}
