package ru.touchin.roboswag.components.calendar;

public class CalendarHeaderItem implements CalendarItem {

    private final int month;
    private final int startRange;
    private final int endRange;

    public CalendarHeaderItem(final int month, final int startRange, final int endRange) {
        this.month = month;
        this.startRange = startRange;
        this.endRange = endRange;
    }

    public int getMonth() {
        return month;
    }

    @Override
    public int getStartRange() {
        return startRange;
    }

    @Override
    public int getEndRange() {
        return endRange;
    }

}