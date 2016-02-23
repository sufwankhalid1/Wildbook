package org.ecocean.search;

import java.time.LocalDate;

public class DateSearch {
    public LocalDate startdate;
    public LocalDate enddate;
    public DateRange range;

    public LocalDate getStartdate() {
        return startdate;
    }
    public void setStartdate(final LocalDate startdate) {
        this.startdate = startdate;
    }
    public LocalDate getEnddate() {
        return enddate;
    }
    public void setEnddate(final LocalDate enddate) {
        this.enddate = enddate;
    }
    public DateRange getRange() {
        return range;
    }
    public void setRange(final DateRange range) {
        this.range = range;
    }
}
