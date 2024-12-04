package com.sirvja.tuntikirjaus;

import com.sirvja.tuntikirjaus.model.HourRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class TestUtils {

    public static List<HourRecord> createDummyHourRecords(int amount) {
        HourRecord firstHourRecord = generateHourRecord(1, LocalDateTime.now());
        UnaryOperator<HourRecord> nextHourRecords = hourRecord -> generateHourRecord(
                hourRecord.getId() + 1,
                hourRecord.getEndTime().orElseThrow(() -> new RuntimeException("Missing end time, shouldn't miss"))
        );

        // Creates infinite (limited by amount) stream of hour records, where previous hour record's end time is always start time of the next record, hour records id is + 1 compared to previous
        return Stream.iterate(firstHourRecord, nextHourRecords)
                .limit(amount)
                .toList();
    }

    public static HourRecord generateHourRecord(int id, LocalDateTime startTime){
        int randomHours = getRandomIntFromRange(0,3);
        int randomMinutes = getRandomIntFromRange(0,60);

        return new HourRecord(
                id,
                startTime,
                startTime.plusHours(randomHours).plusMinutes(randomMinutes),
                String.format("Topic for work that lasts %s hours and %s minutes", randomHours, randomMinutes),
                true
        );
    }

    /**
     * @param min>0
     * @param min<max<1000
     */
    private static int getRandomIntFromRange(int min, int max) {
        validateMinAndMax(min, max);
        double randomInRange = min + (Math.random() * (max - min));
        return (int) Math.round(randomInRange);
    }

    private static void validateMinAndMax(int min, int max) {
        if(max >1000) {
            throw new IllegalArgumentException("max can't be greater than 1000");
        }
        if(min <0) {
            throw new IllegalArgumentException("max can't be smaller than 1000");
        }
        if(min > max) {
            throw new IllegalArgumentException("min value in range can't be greater than max value");
        }
    }
}
