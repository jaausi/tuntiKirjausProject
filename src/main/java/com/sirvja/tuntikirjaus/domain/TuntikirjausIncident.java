package com.sirvja.tuntikirjaus.domain;

import java.time.LocalDateTime;

public record TuntikirjausIncident(
        LocalDateTime localDateTime,
        Incident incident
) {}
