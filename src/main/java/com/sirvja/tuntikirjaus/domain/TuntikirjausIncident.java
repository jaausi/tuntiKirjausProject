package com.sirvja.tuntikirjaus.domain;

import java.time.LocalDateTime;

public record TuntikirjausIncident(
        LocalDateTime time,
        Incident incident
) {
    public LocalDateTime getTime() {
        return time;
    }

    public Incident getIncident() {
        return incident;
    }
}
