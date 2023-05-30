package com.possible.dhis2int.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

public class PharmacySchedule extends Schedule {
    private final Logger logger = getLogger(PharmacySchedule.class);
    private List <PharmacyPeriod> periods;
    private int id;
    private int dhis2ScheduleId;
    private int period;
    private String createdBy;
    private LocalDate createdDate;
    private String lastRun;
    private String status;
    private boolean enabled;
    private String targetDate;

    public List<PharmacyPeriod> getPeriods() {
        return periods;
    }

    public void setTargetDateInit(){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate currentDate = LocalDate.now();
        int currYear = currentDate.getYear();
        int currMonth = currentDate.getMonthValue();
        int currDay = currentDate.getDayOfMonth();
        Boolean found=false;
        for(int i=0;i<periods.size();i++){
            LocalDate period = LocalDate.parse(periods.get(i).getStartTime(), formatter);
            if(!periods.get(i).getStartTime().isEmpty() && !found){ 
                if(currYear==period.getYear()){
                    if(currMonth==period.getMonthValue() && currDay<period.getDayOfMonth()){
                        targetDate=periods.get(i).getStartTime();
                        logger.info("[Found the next date in line ...]"+period.format(formatter));
                        found=true;
                    }else if(currMonth<period.getMonthValue()){
                        targetDate=periods.get(i).getStartTime();
                        logger.info("[Found the next date in line ...]"+period.format(formatter));
                        found=true;
                    }
                }
            }
        }
    }

    public String getTargetDate(){
        return this.targetDate;
    }
    public void setPeriods(List<PharmacyPeriod> periods) {
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

    public Boolean getEnabled(){
        return enabled;
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
}
