package com.sirvja.tuntikirjaus;

import javafx.beans.property.SimpleStringProperty;

public class TableTuntiKirjaus {
    private final SimpleStringProperty time;
    private final SimpleStringProperty topic;
    private final SimpleStringProperty duration;

    public TableTuntiKirjaus(String time, String topic, String duration) {
        this.time = new SimpleStringProperty(time);
        this.topic = new SimpleStringProperty(topic);
        this.duration = new SimpleStringProperty(duration);
    }

    public String getTime() {
        return time.get();
    }

    public void setTime(String time) {
        this.time.set(time);
    }

    public String getTopic() {
        return topic.get();
    }

    public void setTopic(String topic) {
        this.topic.set(topic);
    }

    public String getDuration() {
        return duration.get();
    }

    public void setDuration(String duration) {
        this.duration.set(duration);
    }

    public int getIntDuration(){
        String durationString = duration.get();
        int hours = Integer.parseInt(durationString.split(":")[0]);
        int minutes = Integer.parseInt(durationString.split(":")[1]);

        int durationInMinutes = hours*60 + minutes;
        return durationInMinutes;
    }

    public static String durationOfMinutes(int minutes){
        int hours = minutes/60;
        int minutesLeft = minutes - (hours*60);

        return hours+":"+minutesLeft;
    }

    public boolean isDurationEmpty(){
        return duration.get().isEmpty();
    }
}
