package com.sirvja.tuntikirjaus.model;

import java.time.LocalDate;
import java.util.Optional;

public class ReportConfig implements Comparable<ReportConfig>{

    private int id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String searchQuery;
    private String reportName;

    public ReportConfig(){
    }

    public ReportConfig(int id, LocalDate startDate, LocalDate endDate, String searchQuery, String reportName){
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.searchQuery = searchQuery;
        this.reportName = reportName;
    }

    public ReportConfig(LocalDate startDate, LocalDate endDate, String searchQuery, String reportName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.searchQuery = searchQuery;
        this.reportName = reportName;
    }

    @Override
    public int compareTo(ReportConfig reportConfig) {
        return reportConfig.reportName.compareTo(this.reportName);
    }

    public int getId() {
        return id;
    }

    public Optional<LocalDate> getStartDate() {
        return Optional.ofNullable(startDate);
    }

    public Optional<LocalDate> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public String getReportName() {
        return reportName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString(){
        return this.reportName;
    }
}
