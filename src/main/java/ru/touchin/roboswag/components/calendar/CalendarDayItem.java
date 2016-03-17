package ru.touchin.roboswag.components.calendar;

public class CalendarDayItem implements CalendarItem {

    private final long firstDayReal;
    private final int firstDayInMonth;
    private final int startRange;
    private final int endRange;

    public CalendarDayItem(final long firstDayReal, final int firstDayInMonth, final int startRange, final int endRange) {
        this.firstDayReal = firstDayReal;
        this.firstDayInMonth = firstDayInMonth;
        this.startRange = startRange;
        this.endRange = endRange;
    }

    public long getDateOfFirstDay() {
        return firstDayReal;
    }

    public int getPositionOfFirstDay() {
        return firstDayInMonth;
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