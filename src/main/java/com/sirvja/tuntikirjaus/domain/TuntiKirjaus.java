package com.sirvja.tuntikirjaus.domain;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Function;

public class TuntiKirjaus implements Comparable<TuntiKirjaus>{
    private int id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String topic;
    private boolean isRemote;

    // JavaFX observable properties for TableView binding
    private final ObjectProperty<LocalTime> timeProperty = new SimpleObjectProperty<>();
    private final StringProperty topicProperty = new SimpleStringProperty();
    private final StringProperty durationStringProperty = new SimpleStringProperty();
    private final BooleanProperty remoteProperty = new SimpleBooleanProperty();

    //**************** CONSTRUCTORS *****************//
    public TuntiKirjaus(LocalDateTime startTime, LocalDateTime endTime, String topic, boolean isRemote) {
        assert startTime != null;
        assert topic != null;

        this.startTime = startTime;
        this.endTime = endTime;
        this.topic = topic;
        this.isRemote = isRemote;
        syncProperties();
    }

    private void syncProperties() {
        timeProperty.set(LocalTime.of(startTime.getHour(), startTime.getMinute()));
        topicProperty.set(topic);
        durationStringProperty.set(getDurationString());
        remoteProperty.set(isRemote);
    }

    public TuntiKirjaus(int id, LocalDateTime startTime, LocalDateTime endTime, String topic, boolean isRemote) {
        this(startTime, endTime, topic, isRemote);
        this.id = id;
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

    public boolean isRemote() {
        return isRemote;
    }



    //**************** Methods for tableview *****************//
    public LocalTime getTime() {
        return LocalTime.of(startTime.getHour(), startTime.getMinute());
    }
    public ObjectProperty<LocalTime> timeProperty() {
        return timeProperty;
    }
    public LocalDateTime getDateTime(){
        return startTime;
    }
    public void setTime(LocalTime localTime){
        this.startTime = LocalDateTime.of(LocalDate.now(), localTime);
        timeProperty.set(localTime);
        durationStringProperty.set(getDurationString());
    }
    public String getTopic() {
        return topic;
    }
    public StringProperty topicProperty() {
        return topicProperty;
    }
    public void setTopic(String topic) {
        this.topic = topic;
        topicProperty.set(topic);
    }
    public StringProperty durationStringProperty() {
        return durationStringProperty;
    }
    public BooleanProperty remoteProperty() {
        return remoteProperty;
    }
    public String getDurationString() {
        if(isEndTimeNull()){
            return "-";
        }
        return durationToString.apply(getDurationInDuration());
    }
    public void setRemote(boolean remote) {
        isRemote = remote;
        remoteProperty.set(remote);
    }

    @Override
    public String toString(){
        return String.format("{\n\tid: %s\n", getId()) +
                String.format("\tstart time: %s\n", getStartTime()) +
                String.format("\tend time: %s\n", getEndTime().map(LocalDateTime::toString).orElse("-")) +
                String.format("\ttopic: %s\n}", getTopic());
    }

    private final Function<Duration, String> durationToString = duration -> String.format("%01d:%02d", duration.toHours(), duration.toMinutesPart());
}
