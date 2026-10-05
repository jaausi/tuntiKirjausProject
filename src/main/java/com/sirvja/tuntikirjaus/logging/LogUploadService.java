package com.sirvja.tuntikirjaus.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Sends the application's log files to a server chosen by the user, to help with troubleshooting.
 * The logs of the last {@link #DAYS_TO_SEND} days are zipped and sent with a single HTTP POST (Content-Type application/zip).
 * Nothing is sent automatically: an upload happens only when the user asks for it and has configured the server address.
 * The log files are already masked by {@link MaskingPatternLayout}, and each line is masked again before sending.
 */
public class LogUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogUploadService.class);

    public static final String UPLOAD_URL_KEY = "logUpload.url";
    static final int DAYS_TO_SEND = 7;
    private static final long MAX_UPLOAD_BYTES = 20L * 1024 * 1024;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "[::1]");

    private final HttpClient httpClient;
    private final Path logDirectory;

    public LogUploadService() {
        this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), LogFiles.logDirectory());
    }

    LogUploadService(HttpClient httpClient, Path logDirectory) {
        this.httpClient = httpClient;
        this.logDirectory = logDirectory;
    }

    /**
     * Checks that the address is usable for sending logs. Only https is allowed, except http to the local machine.
     *
     * @return the validated address
     * @throws LogUploadException with a message that can be shown to the user
     */
    public static URI validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new LogUploadException("Lokien lähetysosoitetta ei ole asetettu.");
        }
        URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new LogUploadException("Lokien lähetysosoite ei ole kelvollinen.");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        if (uri.getHost() == null || !(scheme.equals("https") || scheme.equals("http"))) {
            throw new LogUploadException("Lokien lähetysosoitteen tulee alkaa https://");
        }
        if (scheme.equals("http") && !LOCAL_HOSTS.contains(uri.getHost().toLowerCase())) {
            throw new LogUploadException("Lokit lähetetään vain salattua yhteyttä (https) käyttäen.");
        }
        if (uri.getUserInfo() != null) {
            throw new LogUploadException("Älä kirjoita tunnuksia lokien lähetysosoitteeseen.");
        }
        return uri;
    }

    /**
     * Zips the recent log files and posts them to the given address.
     *
     * @throws LogUploadException if there are no logs, the logs are too big or the server doesn't accept them
     */
    public void uploadLogs(String url) {
        URI uri = validateUrl(url);
        List<Path> logFiles = findRecentLogFiles();
        if (logFiles.isEmpty()) {
            throw new LogUploadException("Lähetettäviä lokitiedostoja ei löytynyt.");
        }
        byte[] zip = zipLogFiles(logFiles);
        // The address may contain a path or query that identifies the user, so only the host is logged
        LOGGER.info("Sending {} log file(s) ({} bytes) to host {}", logFiles.size(), zip.length, uri.getHost());

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .header("Content-Type", "application/zip")
                .POST(HttpRequest.BodyPublishers.ofByteArray(zip))
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() / 100 != 2) {
                LOGGER.warn("Log upload failed, server responded with status {}", response.statusCode());
                throw new LogUploadException("Palvelin ei hyväksynyt lokeja (HTTP " + response.statusCode() + ").");
            }
            LOGGER.info("Log upload succeeded with status {}", response.statusCode());
        } catch (IOException e) {
            LOGGER.warn("Log upload failed: {}", e.toString());
            throw new LogUploadException("Lokien lähetys epäonnistui: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LogUploadException("Lokien lähetys keskeytettiin.", e);
        }
    }

    List<Path> findRecentLogFiles() {
        if (!Files.isDirectory(logDirectory)) {
            return List.of();
        }
        Instant since = Instant.now().minus(Duration.ofDays(DAYS_TO_SEND));
        try (Stream<Path> files = Files.list(logDirectory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(file -> {
                        String name = file.getFileName().toString();
                        return name.startsWith("tuntikirjaus") && name.endsWith(".log");
                    })
                    .filter(file -> isModifiedAfter(file, since))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .toList();
        } catch (IOException e) {
            throw new LogUploadException("Lokitiedostojen lukeminen epäonnistui: " + e.getMessage(), e);
        }
    }

    byte[] zipLogFiles(List<Path> logFiles) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Path file : logFiles) {
                zip.putNextEntry(new ZipEntry(file.getFileName().toString()));
                // Mask again in case the file was written by an older version that didn't mask its logs
                try (Stream<String> lines = Files.lines(file)) {
                    for (String line : (Iterable<String>) lines::iterator) {
                        zip.write((LogMasker.mask(line) + "\n").getBytes(StandardCharsets.UTF_8));
                    }
                }
                zip.closeEntry();
                if (bytes.size() > MAX_UPLOAD_BYTES) {
                    throw new LogUploadException("Lokitiedostot ovat liian suuria lähetettäväksi.");
                }
            }
        } catch (IOException e) {
            throw new LogUploadException("Lokitiedostojen pakkaaminen epäonnistui: " + e.getMessage(), e);
        }
        return bytes.toByteArray();
    }

    private static boolean isModifiedAfter(Path file, Instant since) {
        try {
            return Files.getLastModifiedTime(file).toInstant().isAfter(since);
        } catch (IOException e) {
            return false;
        }
    }
}
