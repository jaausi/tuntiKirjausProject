package com.sirvja.tuntikirjaus.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HourRecordTest {

    @Test
    public void testGetDurationString(){
        HourRecord hourRecord = new HourRecord(LocalDateTime.now().minusHours(5).minusDays(2), LocalDateTime.now().minusHours(6).minusDays(2), "IBD-1220 Koodaus", true);
        String durationString = hourRecord.getDurationString();

        assertEquals("0:59", durationString);
    }
    final LocalDateTime zeroDateTime = LocalDateTime.of(
            LocalDate.of(2023, 1, 1),
            LocalTime.of(0,0,0)
    );


    @Test
    void givenTuntiKirjaus_whenGetDurationString_thenReturnStringInCorrectFormat(){
        // given
        HourRecord oneHour = tuntiKirjausOfDuration.apply(1L, 0L);
        HourRecord twelveHours = tuntiKirjausOfDuration.apply(12L, 0L);
        HourRecord oneMinute = tuntiKirjausOfDuration.apply(0L, 1L);
        HourRecord twelveMinutes = tuntiKirjausOfDuration.apply(0L, 12L);

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
        HourRecord oawLounas = tuntiKirjausOfTopic.apply("oaw Lounas");
        HourRecord tennis = tuntiKirjausOfTopic.apply("Tennis");
        HourRecord ibd = tuntiKirjausOfTopic.apply("IBD-1221 Koodaus");

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
    final BiFunction<Long, Long, HourRecord> tuntiKirjausOfDuration = (hours, minutes) -> new HourRecord(
            zeroDateTime,
            zeroDateTime.plusHours(hours).plusMinutes(minutes),
            "IBD-1221 Koodaus",
            true
    );
    final Function<String, HourRecord> tuntiKirjausOfTopic = topic -> new HourRecord(
            zeroDateTime,
            zeroDateTime.plusHours(1).plusMinutes(1),
            topic,
            true
    );
}
