package com.sirvja.tuntikirjaus.logging;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class LogUploadServiceTest {

    @TempDir
    Path logDir;

    private HttpServer server;
    private final AtomicReference<byte[]> receivedBody = new AtomicReference<>();
    private final AtomicReference<String> receivedContentType = new AtomicReference<>();
    private int responseStatus = 200;
    private LogUploadService service;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/logs", exchange -> {
            receivedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            receivedBody.set(exchange.getRequestBody().readAllBytes());
            exchange.sendResponseHeaders(responseStatus, -1);
            exchange.close();
        });
        server.start();
        service = new LogUploadService(HttpClient.newHttpClient(), logDir);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private String serverUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/logs";
    }

    @Test
    void uploadsRecentLogFilesAsMaskedZip() throws IOException {
        Files.writeString(logDir.resolve("tuntikirjaus.log"), "today password=hunter2\n");
        Files.writeString(logDir.resolve("tuntikirjaus.2026-01-01.0.log"), "old\n");
        Files.setLastModifiedTime(logDir.resolve("tuntikirjaus.2026-01-01.0.log"),
                FileTime.from(Instant.now().minus(Duration.ofDays(LogUploadService.DAYS_TO_SEND + 1))));
        Files.writeString(logDir.resolve("other.txt"), "not a log\n");

        service.uploadLogs(serverUrl());

        assertEquals("application/zip", receivedContentType.get());
        Map<String, String> entries = unzip(receivedBody.get());
        assertEquals(List.of("tuntikirjaus.log"), List.copyOf(entries.keySet()));
        assertEquals("today password=****\n", entries.get("tuntikirjaus.log"));
    }

    @Test
    void failsWhenServerRejectsLogs() throws IOException {
        Files.writeString(logDir.resolve("tuntikirjaus.log"), "line\n");
        responseStatus = 500;

        LogUploadException e = assertThrows(LogUploadException.class, () -> service.uploadLogs(serverUrl()));
        assertTrue(e.getMessage().contains("500"));
    }

    @Test
    void failsWhenThereAreNoLogs() {
        assertThrows(LogUploadException.class, () -> service.uploadLogs(serverUrl()));
        assertNull(receivedBody.get());
    }

    @Test
    void acceptsOnlyHttpsOrLocalHttp() {
        assertDoesNotThrow(() -> LogUploadService.validateUrl("https://logs.example.com/upload"));
        assertDoesNotThrow(() -> LogUploadService.validateUrl("http://localhost:8080/logs"));
        assertThrows(LogUploadException.class, () -> LogUploadService.validateUrl("http://logs.example.com/upload"));
        assertThrows(LogUploadException.class, () -> LogUploadService.validateUrl("ftp://logs.example.com"));
        assertThrows(LogUploadException.class, () -> LogUploadService.validateUrl("https://user:pass@logs.example.com"));
        assertThrows(LogUploadException.class, () -> LogUploadService.validateUrl(""));
        assertThrows(LogUploadException.class, () -> LogUploadService.validateUrl(null));
    }

    private static Map<String, String> unzip(byte[] zip) throws IOException {
        Map<String, String> entries = new HashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                entries.put(entry.getName(), new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entries;
    }
}
