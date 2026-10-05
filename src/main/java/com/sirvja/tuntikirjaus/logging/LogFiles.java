package com.sirvja.tuntikirjaus.logging;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Location of the application's log files. Keep in sync with LOG_DIR in logback.xml.
 */
public final class LogFiles {

    public static final String LOG_DIR_PROPERTY = "tuntikirjaus.log.dir";

    private LogFiles() {
    }

    public static Path logDirectory() {
        String configured = System.getProperty(LOG_DIR_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("user.home"), "tuntikirjaus", "logs");
    }

    public static Path currentLogFile() {
        return logDirectory().resolve("tuntikirjaus.log");
    }
}
