package com.sirvja.tuntikirjaus.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TuntiKirjausTest {

    @Test
    public void testGetDurationString(){
        TuntiKirjaus tuntiKirjaus = new TuntiKirjaus(LocalDateTime.now().minusHours(5).minusDays(2), LocalDateTime.now().minusHours(6).minusDays(2), "IBD-1220 Koodaus", true);
        String durationString = tuntiKirjaus.getDurationString();

        assertEquals("0:59", durationString);
    }
}
