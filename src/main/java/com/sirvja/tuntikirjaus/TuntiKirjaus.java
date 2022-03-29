package com.sirvja.tuntikirjaus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.InputMismatchException;

public class TuntiKirjaus implements Comparable<TuntiKirjaus>, Cloneable {
    private LocalDateTime time;
    private String topic;
    private Duration duration;
    private Boolean durationEnabled;

    public Boolean getDurationEnabled() {
        return durationEnabled;
    }

    public void setDurationEnabled(Boolean durationEnabled) {
        this.durationEnabled = durationEnabled;
    }

    public TuntiKirjaus(LocalDateTime time, String topic) {
        this.time = time;
        this.topic = topic;
        this.durationEnabled = true;
    }

    public TuntiKirjaus(LocalDateTime time, String topic, Duration duration) {
        this.time = time;
        this.topic = topic;
        this.duration = duration;
    }

    public TuntiKirjaus(LocalDateTime time, String topic, Boolean durationEnabled) {
        this.time = time;
        this.topic = topic;
        this.durationEnabled = durationEnabled;
    }

    public TuntiKirjaus(LocalDateTime time, String topic, Duration duration, Boolean durationEnabled) {
        this.time = time;
        this.topic = topic;
        this.duration = duration;
        this.durationEnabled = durationEnabled;
    }


    @Override
    public int compareTo(TuntiKirjaus tuntiKirjaus){
        return this.time.compareTo(tuntiKirjaus.time);
    }

    @Override
    public TuntiKirjaus clone() {
        return new TuntiKirjaus(
                this.time,
                this.topic,
                this.duration,
                this.durationEnabled
        );
    }

    public boolean isDurationNull(){
        return this.duration == null;
    }

    public LocalTime getTime() {
        return LocalTime.of(time.getHour(), time.getMinute());
    }

    public LocalDateTime getDateTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setDuration(String duration) throws InputMismatchException {
        int[] durationArray = getDurationFromString(duration);
        this.duration = Duration.ofHours(durationArray[0]).plusMinutes(durationArray[1]);
    }

    private int[] getDurationFromString(String duration) throws InputMismatchException{
        if(!duration.contains(":")) throw new InputMismatchException("Duration string doesn't contain ':'");
        int[] durationArray = new int[2];
        String[] splittedArray = duration.split(":");
        durationArray[0] = Integer.parseInt(splittedArray[0]);
        durationArray[1] = Integer.parseInt(splittedArray[1]);

        return durationArray;
    }
}
