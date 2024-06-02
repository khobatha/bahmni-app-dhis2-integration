package com.possible.dhis2int.scheduler;

import java.util.List;

public class PharmacyPeriodListRequest {
    private List<PharmacyPeriodReq> periods;

    public List<PharmacyPeriodReq> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PharmacyPeriodReq> periods) {
        this.periods = periods;
    }
}
