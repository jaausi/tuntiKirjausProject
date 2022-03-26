package com.sirvja.tuntikirjaus;

import java.time.Duration;
import java.time.LocalTime;
import java.util.InputMismatchException;

public class TuntiKirjaus {
    private LocalTime time;
    private String topic;
    private Duration duration;

    public TuntiKirjaus(LocalTime time, String topic) {
        this.time = time;
        this.topic = topic;
    }

    public TuntiKirjaus(String time, String topic) {
        int[] timeArray = getDurationFromString(time);
        this.time = LocalTime.of(timeArray[0], timeArray[1]);
        this.topic = topic;
    }

    public TuntiKirjaus(LocalTime time, String topic, Duration duration){
        this.time = time;
        this.topic = topic;
        this.duration = duration;
    }

    public String getTime() {
        return time.toString();
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDuration() {
        return duration.toString();
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
