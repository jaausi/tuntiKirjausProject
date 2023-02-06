package com.sirvja.tuntikirjaus.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TuntiKirjausTest {

    @Test
    public void testGetDurationString(){
        TuntiKirjaus tuntiKirjaus = new TuntiKirjaus(LocalDateTime.now().minusHours(5).minusDays(2), LocalDateTime.now().minusHours(6).minusDays(2), "IBD-1220 Koodaus", true);
        String durationString = tuntiKirjaus.getDurationString();

        assertEquals("0:59", durationString);
    }
    final LocalDateTime zeroDateTime = LocalDateTime.of(
            LocalDate.of(2023, 1, 1),
            LocalTime.of(0,0,0)
    );


    @Test
    void givenTuntiKirjaus_whenGetDurationString_thenReturnStringInCorrectFormat(){
        // given
        TuntiKirjaus oneHour = tuntiKirjausOfDuration.apply(1L, 0L);
        TuntiKirjaus twelveHours = tuntiKirjausOfDuration.apply(12L, 0L);
        TuntiKirjaus oneMinute = tuntiKirjausOfDuration.apply(0L, 1L);
        TuntiKirjaus twelveMinutes = tuntiKirjausOfDuration.apply(0L, 12L);

        // when
        String durationString_oneHour = oneHour.getDurationString();
        String durationString_12Hours = twelveHours.getDurationString();
        String durationString_oneMinute = oneMinute.getDurationString();
        String durationString_12Minutes = twelveMinutes.getDurationString();

        // then
        assertEquals("1:00", durationString_oneHour);
        assertEquals("12:00", durationString_12Hours);
        assertEquals("0:01", durationString_oneMinute);
        assertEquals("0:12", durationString_12Minutes);
    }

    @Test
    void givenTuntiKirjaus_whenGetClassification_thenReturnClassificationInString(){
        // given
        TuntiKirjaus oawLounas = tuntiKirjausOfTopic.apply("oaw Lounas");
        TuntiKirjaus tennis = tuntiKirjausOfTopic.apply("Tennis");
        TuntiKirjaus ibd = tuntiKirjausOfTopic.apply("IBD-1221 Koodaus");

        // when
        String oawClassification = oawLounas.getClassification();
        String tennisClassification = tennis.getClassification();
        String ibdClassification = ibd.getClassification();

        // then
        assertEquals("Other admin work", oawClassification);
        assertEquals("Tennis", tennisClassification);
        assertEquals("IBD", ibdClassification);
    }


    /*********** HELPER FUNCTIONS ***********/
    final BiFunction<Long, Long, TuntiKirjaus> tuntiKirjausOfDuration = (hours, minutes) -> new TuntiKirjaus(
            zeroDateTime,
            zeroDateTime.plusHours(hours).plusMinutes(minutes),
            "IBD-1221 Koodaus",
            true
    );
    final Function<String, TuntiKirjaus> tuntiKirjausOfTopic = topic -> new TuntiKirjaus(
            zeroDateTime,
            zeroDateTime.plusHours(1).plusMinutes(1),
            topic,
            true
    );
}
