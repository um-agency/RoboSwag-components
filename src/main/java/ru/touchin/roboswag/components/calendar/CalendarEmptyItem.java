package ru.touchin.roboswag.components.calendar;

public class CalendarEmptyItem implements CalendarItem {

    private final int startRange;
    private final int endRange;

    public CalendarEmptyItem(final int startRange, final int endRange) {
        this.startRange = startRange;
        this.endRange = endRange;
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