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
    private Boolean durationEnabled;
    private Duration duration;

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

    @Override
    public int compareTo(TuntiKirjaus tuntiKirjaus){
        return this.startTime.compareTo(tuntiKirjaus.startTime);
    }

    public Boolean getDurationEnabled() {
        return durationEnabled;
    }

    public void setDurationEnabled(Boolean durationEnabled) {
        this.durationEnabled = durationEnabled;
    }

    public boolean isEndTimeNull(){
        return this.endTime == null;
    }

    public boolean isDurationEnabled(){
        return this.durationEnabled;
    }

    public LocalTime getTime() {
        return LocalTime.of(startTime.getHour(), startTime.getMinute());
    }

    public void setTime(LocalTime localTime){
        this.startTime = LocalDateTime.of(LocalDate.now(), localTime);
    }

    public String getTopic() {
        return topic;
    }

    public String getClassification(){
        String prefix = topic.split(" ")[0].toUpperCase();

        String classification = null;
        if ("OAW".equals(prefix)) {
            classification = "Other admin work";
        } else if(prefix.contains("-")) {
            classification = getProjectFromJiraCode(prefix);
        } else {
            classification = prefix;
        }

        return classification;
    }

    private String getProjectFromJiraCode(String prefix) {
        return prefix.split("-")[0];
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDuration() {
        return DurationFormatUtils.formatDuration(getDurationInDuration().toMillis(), "H:mm");
    }

    public Duration getDurationInDuration() {
        if(this.endTime != null){
            return Duration.between(this.startTime, this.endTime).abs();
        }
        return Duration.ZERO;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
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
        this.duration = Duration.between(this.startTime, this.endTime).abs();
    }

    public LocalDate getLocalDateOfStartTime(){
        return this.startTime.toLocalDate();
    }

    public int getId() {
        return id;
    }

    public void setId(int rowid) {
        id = rowid;
    }
}
