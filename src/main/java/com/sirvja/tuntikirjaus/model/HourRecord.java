package com.sirvja.tuntikirjaus.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.sirvja.tuntikirjaus.utils.Constants.dateTimeFormatter;

public class HourRecord implements Comparable<HourRecord>{
    private int id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String topic;
    private boolean durationEnabled;

    //**************** CONSTRUCTORS *****************//
    public HourRecord(LocalDateTime startTime, LocalDateTime endTime, String topic, Boolean durationEnabled) {
        assert startTime != null;
        assert topic != null;
        assert durationEnabled != null;

        this.startTime = startTime;
        this.endTime = endTime;
        this.topic = topic;
        this.durationEnabled = durationEnabled;
    }
    public HourRecord(int id, LocalDateTime startTime, LocalDateTime endTime, String topic, Boolean durationEnabled) {
        assert startTime != null;
        assert topic != null;
        assert durationEnabled != null;

        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.topic = topic;
        this.durationEnabled = durationEnabled;
    }

    public static HourRecord of(ResultSet rs, int rowNumber) throws SQLException {
        return new HourRecord(
                rs.getInt("ROWID"),
                LocalDateTime.parse(rs.getString("START_TIME"), dateTimeFormatter),
                LocalDateTime.parse(rs.getString("END_TIME"), dateTimeFormatter),
                rs.getString("TOPIC"),
                Boolean.parseBoolean(rs.getString("DURATION_ENABLED"))
        );
    }

    public static HourRecord of(int id, HourRecord hourRecord) {
        return new HourRecord(
                id,
                hourRecord.getStartTime(),
                hourRecord.getEndTime().orElse(null),
                hourRecord.getTopic(),
                hourRecord.isDurationEnabled()
        );
    }

    //**************** Common methods for object *****************//
    @Override
    public int compareTo(HourRecord hourRecord){
        return this.startTime.compareTo(hourRecord.startTime);
    }

    public int getId() {
        return id;
    }
    public void setId(int rowid) {
        id = rowid;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Optional<LocalDateTime> getEndTime() {
        return Optional.ofNullable(endTime);
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public boolean isEndTimeNull(){
        return getEndTime().isEmpty();
    }

    public boolean isDurationEnabled(){
        return this.durationEnabled;
    }
    public void setDurationEnabled(boolean durationEnabled) {
        this.durationEnabled = durationEnabled;
    }

    public Duration getDurationInDuration() {
        Optional<LocalDateTime> optionalEndTime = getEndTime();
        if(optionalEndTime.isPresent()){
            return Duration.between(this.startTime, optionalEndTime.get()).abs();
        }
        return Duration.ZERO;
    }

    public String getClassification(){
        String prefix = topic.split(" ")[0].toUpperCase();

        String classification = null;
        if ("OAW".equalsIgnoreCase(prefix)) {
            classification = "Other admin work";
        } else if(prefix.contains("-")) {
            classification = getProjectFromJiraCode(prefix);
        } else {
            classification = topic;
        }

        return classification;
    }
    private String getProjectFromJiraCode(String prefix) {
        return prefix.split("-")[0];
    }

    public LocalDate getLocalDateOfStartTime(){
        return this.startTime.toLocalDate();
    }


    //**************** Methods for tableview *****************//
    public LocalTime getTime() {
        return LocalTime.of(startTime.getHour(), startTime.getMinute());
    }
    public LocalDateTime getDateTime(){
        return startTime;
    }
    public void setTime(LocalTime localTime){
        this.startTime = LocalDateTime.of(LocalDate.now(), localTime);
    }
    public String getTopic() {
        return topic;
    }
    public void setTopic(String topic) {
        this.topic = topic;
    }
    public String getDurationString() {
        if(isEndTimeNull() || !isDurationEnabled()){
            return "-";
        }
        return durationToString.apply(getDurationInDuration());
    }

    @Override
    public String toString(){
        return String.format("{\n\tid: %s\n", getId()) +
                String.format("\tstart time: %s\n", getStartTime()) +
                String.format("\tend time: %s\n", getEndTime().map(LocalDateTime::toString).orElse("-")) +
                String.format("\ttopic: %s\n}", getTopic());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HourRecord that = (HourRecord) o;

        if (id != that.id) return false;
        if (durationEnabled != that.durationEnabled) return false;
        if (!startTime.equals(that.startTime)) return false;
        if (!Objects.equals(endTime, that.endTime)) return false;
        return topic.equals(that.topic);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + startTime.hashCode();
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + topic.hashCode();
        return result;
    }

    private final Function<Duration, String> durationToString = duration -> String.format("%01d:%02d", duration.toHours(), duration.toMinutesPart());
}
