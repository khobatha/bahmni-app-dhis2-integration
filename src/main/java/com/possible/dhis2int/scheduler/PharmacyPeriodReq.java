package com.possible.dhis2int.scheduler;

import java.time.LocalDate;

public class PharmacyPeriodReq {
    
    private String start;
    private String end;

    PharmacyPeriodReq(){
        start="";
        end="";
    }
    PharmacyPeriodReq(String startTime, String endTime){
        start=startTime;
        end=endTime;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String startTime) {
        this.start = startTime;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String endTime) {
        this.end = endTime;
    }
    
}
