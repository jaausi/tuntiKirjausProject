package com.sirvja.tuntikirjaus.domain;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class TuntiKirjaus implements Comparable<TuntiKirjaus>{
    private int id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String topic;
    private boolean durationEnabled;

    //**************** CONSTRUCTORS *****************//
    public TuntiKirjaus(LocalDateTime startTime, LocalDateTime endTime, String topic, Boolean durationEnabled) {
        assert startTime != null;
        assert topic != null;
        assert durationEnabled != null;

        this.startTime = startTime;
        this.endTime = endTime;
        this.topic = topic;
        this.durationEnabled = durationEnabled;
    }
    public TuntiKirjaus(int id, LocalDateTime startTime, LocalDateTime endTime, String topic, Boolean durationEnabled) {
        assert startTime != null;
        assert topic != null;
        assert durationEnabled != null;

        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.topic = topic;
        this.durationEnabled = durationEnabled;
    }

    //**************** Common methods for object *****************//
    @Override
    public int compareTo(TuntiKirjaus tuntiKirjaus){
        return this.startTime.compareTo(tuntiKirjaus.startTime);
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
        if ("OAW".equals(prefix)) {
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
        return DurationFormatUtils.formatDuration(getDurationInDuration().toMillis(), "H:mm");
    }

    @Override
    public String toString(){
        return String.format("{\n\tid: %s\n", getId()) +
                String.format("\tstart time: %s\n", getStartTime()) +
                String.format("\tend time: %s\n", getEndTime().map(LocalDateTime::toString).orElse("-")) +
                String.format("\ttopic: %s\n}", getTopic());
    }
}
