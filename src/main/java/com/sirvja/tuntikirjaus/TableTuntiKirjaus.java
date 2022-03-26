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
        String minuutitString = minutesLeft < 10 ? "0"+minutesLeft : ""+minutesLeft;

        return hours+":"+minuutitString;
    }

    public boolean isDurationEmpty(){
        return duration.get().isEmpty();
    }

    public int compareTime(String timeB){
        int hoursA = Integer.parseInt(time.get().split(":")[0]);
        int minutesA = Integer.parseInt(time.get().split(":")[1]);
        int hoursB = Integer.parseInt(timeB.split(":")[0]);
        int minutesB = Integer.parseInt(timeB.split(":")[1]);

        if(hoursA > hoursB) return 1;
        else if(hoursB > hoursA) return -1;
        else if(minutesA > minutesB) return 1;
        else if (minutesB > minutesA) return -1;
        else return 0;
    }
}
