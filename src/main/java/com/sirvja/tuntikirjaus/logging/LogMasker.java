package com.sirvja.tuntikirjaus.logging;

import java.util.regex.Pattern;

/**
 * Removes sensitive information from log lines before they are written, so log files can be shared for troubleshooting.
 * Masks the user's home directory (contains the username), email addresses, credentials in URLs,
 * bearer tokens and values of password/token/secret like keys.
 */
public final class LogMasker {

    static final String MASK = "****";

    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern URL_CREDENTIALS = Pattern.compile("(?i)([a-z][a-z0-9+.-]*://)[^/\\s:@]+:[^/\\s@]+@");
    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)(bearer\\s+)[A-Za-z0-9._~+/=-]+");
    private static final Pattern SECRET_VALUE = Pattern.compile(
            "(?i)((?:password|passwd|pwd|salasana|secret|token|api[_-]?key|authorization|session[_-]?id|cookie)\"?\\s*[:=]\\s*\"?)[^\\s\",;&}]+");

    private static volatile String userHome = System.getProperty("user.home");

    private LogMasker() {
    }

    public static String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String masked = text;
        String home = userHome;
        if (home != null && home.length() > 1) {
            masked = masked.replace(home, "~");
        }
        masked = URL_CREDENTIALS.matcher(masked).replaceAll("$1" + MASK + "@");
        masked = BEARER_TOKEN.matcher(masked).replaceAll("$1" + MASK);
        masked = SECRET_VALUE.matcher(masked).replaceAll("$1" + MASK);
        masked = EMAIL.matcher(masked).replaceAll(MASK + "@" + MASK);
        return masked;
    }

    // Tests use a fixed home directory so they don't depend on the machine they run on
    static void setUserHome(String home) {
        userHome = home;
    }
}
