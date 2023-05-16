package com.possible.dhis2int.scheduler;

import java.time.LocalDate;
import java.util.List;

public class PharmacySchedule extends Schedule {
    private List <PharmacyPeriodReq> periods;
    private int id;
    private int dhis2ScheduleId;
    private int period;
    private String createdBy;
    private LocalDate createdDate;
    private String lastRun;
    private String status;
    private boolean enabled;

    public List<PharmacyPeriodReq> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PharmacyPeriodReq> periods) {
        this.periods = periods;
    }

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
