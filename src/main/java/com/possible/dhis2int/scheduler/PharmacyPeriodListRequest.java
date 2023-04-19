package com.possible.dhis2int.scheduler;

import java.util.List;

public class PharmacyPeriodListRequest {
    private List<PharmacyPeriod> periods;

    public List<PharmacyPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PharmacyPeriod> periods) {
        this.periods = periods;
    }
}
