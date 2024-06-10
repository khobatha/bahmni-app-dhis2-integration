package com.possible.dhis2int.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Schedule {
    private int id;
    private String programName;
    private int reportId;
    private String lastRun;
    private String status;
    private LocalDate created_date;
    private LocalDateTime target_date;
    private String created_by;
    private String frequency;
    private Boolean enabled;

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
        return created_by;
    }

    public void setCreatedBy(String created_by) {
        this.created_by = created_by;
    }

    public LocalDate getCreatedDate() {
        return created_date;
    }

    public void setCreatedDate(LocalDate created_date) {
        this.created_date = created_date;
    }

    public LocalDateTime getTargetDate() {
        return target_date;
    }

    public void setTargetDate(LocalDateTime target_date) {
        this.target_date = target_date;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", programName='" + programName + '\'' +
                ", reportId=" + reportId +
                ", lastRun='" + lastRun + '\'' +
                ", status='" + status + '\'' +
                ", created_date=" + created_date +
                ", target_date=" + target_date +
                ", created_by='" + created_by + '\'' +
                ", frequency='" + frequency + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
