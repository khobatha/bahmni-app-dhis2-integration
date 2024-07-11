package com.possible.dhis2int.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Schedule {
    private int id;
    private String programName;
    private int reportId;
    private String lastRun;
    private String status;
    private LocalDate createdDate;
    private LocalDateTime targetDate;
    private String createdBy;
    private String frequency;
    private Boolean enabled;
    private Integer weekStartDay;  // New field

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getLastRun() {
        return lastRun;
    }

    public void setLastRun(String lastRun) {
        this.lastRun = lastRun == null ? "-" : lastRun;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? "Ready" : status;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDateTime targetDate) {
        this.targetDate = targetDate;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getWeekStartDay() {  // New getter
        return weekStartDay;
    }

    public void setWeekStartDay(Integer weekStartDay) {  // New setter
        this.weekStartDay = weekStartDay;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", programName='" + programName + '\'' +
                ", reportId=" + reportId +
                ", lastRun='" + lastRun + '\'' +
                ", status='" + status + '\'' +
                ", createdDate=" + createdDate +
                ", targetDate=" + targetDate +
                ", createdBy='" + createdBy + '\'' +
                ", frequency='" + frequency + '\'' +
                ", enabled=" + enabled +
                ", weekStartDay=" + weekStartDay +  // Include in toString
                '}';
    }
}
