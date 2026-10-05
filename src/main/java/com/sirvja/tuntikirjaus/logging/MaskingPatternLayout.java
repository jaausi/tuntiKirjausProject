package com.sirvja.tuntikirjaus.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Pattern layout that runs the whole formatted log line, including stack traces, through {@link LogMasker}.
 * Used in logback.xml for the log file, which may be sent to a server.
 */
public class MaskingPatternLayout extends PatternLayout {

    @Override
    public String doLayout(ILoggingEvent event) {
        return LogMasker.mask(super.doLayout(event));
    }
}
