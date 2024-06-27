package com.possible.dhis2int.date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ReportDateRange {
	
	private final DateTime startDate;
	
	private final DateTime endDate;
	
	public ReportDateRange(DateTime startDate, DateTime endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public ReportDateRange(String startDateString, String endDateString) {
        String pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);

        try {
            this.startDate = formatter.parseDateTime(startDateString);
            this.endDate = formatter.parseDateTime(endDateString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid date format. Please provide dates in the format 'yyyy-MM-dd'.", e);
        }
    }
	
	public String getStartDate() {
		return DateTimeFormat.forPattern("yyyy-MM-dd").print(startDate);
	}
	
	public String getEndDate() {
		return DateTimeFormat.forPattern("yyyy-MM-dd").print(endDate);
	}

	@Override
	public String toString() {
		return "ReportDateRange [startDate=" + startDate + ", endDate=" + endDate + "]";
	}
	
}
