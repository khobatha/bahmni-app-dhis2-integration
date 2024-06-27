package com.possible.dhis2int.scheduler;

import java.time.LocalDate;

public class PharmacyPeriod {
    private int id;
    private int dhis2ScheduleId;
    private int period;
    private String createdBy;
    private LocalDate createdDate;
    private String startTime;
    private String endTime;
    private String lastRun;
    private String status;
    private boolean enabled;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDhis2ScheduleId() {
        return dhis2ScheduleId;
    }

    public void setDhis2ScheduleId(int dhis2ScheduleId) {
        this.dhis2ScheduleId = dhis2ScheduleId;
    }

    public int getPeriod() {
        return period;
    }

    public boolean getEnabled(){
        return enabled;
    }

    public void setPeriod(int period) {
        this.period = period;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLastRun() {
        return lastRun;
    }

    public void setLastRun(String lastRun) {
        this.lastRun = lastRun;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}
