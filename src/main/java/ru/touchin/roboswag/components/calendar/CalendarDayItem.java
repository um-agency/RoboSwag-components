package ru.touchin.roboswag.components.calendar;

import android.support.annotation.NonNull;

public class CalendarDayItem implements CalendarItem {

    private final long dateOfFirstDay;
    private final int positionOfFirstDate;
    private final int startRange;
    private final int endRange;
    @NonNull
    private final CalendarDateState dateState;

    public CalendarDayItem(final long dateOfFirstDay,
                           final int positionOfFirstDate,
                           final int startRange,
                           final int endRange,
                           @NonNull final CalendarDateState dateState) {
        this.dateOfFirstDay = dateOfFirstDay;
        this.positionOfFirstDate = positionOfFirstDate;
        this.startRange = startRange;
        this.endRange = endRange;
        this.dateState = dateState;
    }

    public long getDateOfFirstDay() {
        return dateOfFirstDay;
    }

    public int getPositionOfFirstDay() {
        return positionOfFirstDate;
    }

    @Override
    public int getStartRange() {
        return startRange;
    }

    @Override
    public int getEndRange() {
        return endRange;
    }

    @NonNull
    public CalendarDateState getDateState() {
        return dateState;
    }

}