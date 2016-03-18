/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.calendar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.touchin.roboswag.core.log.Lc;

/**
 * Created by Ilia Kurtov on 17.03.2016.
 */
public final class CalendarUtils {

    public static final int DAYS_IN_WEEK = 7;

    @Nullable
    public static CalendarItem findItemByPosition(@Nullable final List<CalendarItem> calendarItems, final long position) {
        return find(calendarItems, position, false);
    }

    @Nullable
    public static Integer findPositionOfSelectedMonth(@Nullable final List<CalendarItem> calendarItems, final long position) {
        final CalendarItem calendarItem = find(calendarItems, position, true);
        if (calendarItem != null) {
            return calendarItem.getStartRange();
        }
        return null;
    }

    @Nullable
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
    private static CalendarItem find(@Nullable final List<CalendarItem> calendarItems, final long position, final boolean getHeaderPosition) {
        if (calendarItems != null) {
            int low = 0;
            int high = calendarItems.size() - 1;
            while (true) {
                final int mid = (low + high) / 2;
                if (position < calendarItems.get(mid).getStartRange()) {
                    if (mid == 0 || position > calendarItems.get(mid - 1).getEndRange()) {
                        Lc.assertion("CalendarAdapter cannot find item with that position");
                        break;
                    }
                    high = mid - 1;
                } else if (position > calendarItems.get(mid).getEndRange()) {
                    if (mid == calendarItems.size() || position < calendarItems.get(mid + 1).getStartRange()) {
                        Lc.assertion("CalendarAdapter cannot find item with that position");
                        break;
                    }
                    low = mid + 1;
                } else {
                    if (getHeaderPosition) {
                        int calendarShift = mid;
                        while (true) {
                            calendarShift--;
                            if (calendarShift == -1) {
                                return null;
                            }
                            if (calendarItems.get(calendarShift) instanceof CalendarHeaderItem) {
                                return calendarItems.get(calendarShift);
                            }
                        }
                    }
                    return calendarItems.get(mid);
                }
            }
        } else {
            Lc.assertion("Calendar Items list is empty");
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("checkstyle:MethodLength")
    public static Integer findPositionByDate(@Nullable final List<CalendarItem> calendarItems, final long date) {
        if (calendarItems == null || calendarItems.isEmpty()) {
            Lc.assertion("Calendar Items List is null");
            return null;
        }

        int low = 0;
        int high = calendarItems.size() - 1;
        int addition = 0;
        float count = 0;
        while (true) {
            final int mid = (low + high) / 2 + addition;
            if (calendarItems.get(mid) instanceof CalendarDayItem) {
                if (date < ((CalendarDayItem) calendarItems.get(mid)).getDateOfFirstDay()) {
                    if (mid == 0) {
                        Lc.assertion("Selected date smaller then min date in calendar");
                        break;
                    }
                    high = mid - 1;
                } else {
                    final long endDate = ((CalendarDayItem) calendarItems.get(mid)).getDateOfFirstDay()
                            + calendarItems.get(mid).getEndRange() - calendarItems.get(mid).getStartRange();
                    if (date > endDate) {
                        if (mid == calendarItems.size()) {
                            Lc.assertion("Selected date bigger then max date in calendar");
                            break;
                        }
                        low = mid + 1;
                    } else {
                        return (int) (calendarItems.get(mid).getStartRange()
                                + date - ((CalendarDayItem) calendarItems.get(mid)).getDateOfFirstDay());
                    }
                }
                count = 0;
                addition = 0;
            } else {
                count++;
                addition = ((int) Math.ceil(count / 2)) * ((int) (StrictMath.pow(-1, (count - 1))));
            }
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("checkstyle:MethodLength")
    public static List<CalendarItem> fillRanges(@NonNull final Calendar startDate, @NonNull final Calendar endDate) {
        final Calendar cleanStartDate = getCleanDate(startDate);
        final Calendar cleanEndDate = getCleanDate(endDate);

        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(cleanStartDate.getTime());

        final List<CalendarItem> calendarItems = fillCalendarTillCurrentDate(cleanStartDate, calendar);

        calendar.add(Calendar.DAY_OF_MONTH, 1);

        final int totalDaysCount = (int) ((cleanEndDate.getTimeInMillis() - calendar.getTimeInMillis()) / CalendarAdapter.ONE_DAY_LENGTH + 1);
        int shift = calendarItems.get(calendarItems.size() - 1).getEndRange();
        int firstDate = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        int daysEnded = 1;

        while (true) {
            final int daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            long firstRangeDate = calendar.getTimeInMillis() / CalendarAdapter.ONE_DAY_LENGTH + 1;

            if ((daysEnded + (daysInCurrentMonth - firstDate)) <= totalDaysCount) {
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.setTime(new Date(calendar.getTimeInMillis() + CalendarAdapter.ONE_DAY_LENGTH));

                calendarItems.add(new CalendarDayItem(firstRangeDate, firstDate + 1, shift + daysEnded,
                        shift + daysEnded + (daysInCurrentMonth - firstDate) - 1, CalendarDateState.AFTER_TODAY));
                daysEnded += daysInCurrentMonth - firstDate;
                if (daysEnded == totalDaysCount) {
                    break;
                }
                firstDate = 0;

                final int firstDayInWeek = getFirstDateStart(calendar);

                if (firstDayInWeek != 0) {
                    calendarItems.add(new CalendarEmptyItem(shift + daysEnded, shift + daysEnded + (DAYS_IN_WEEK - firstDayInWeek - 1)));
                    shift += (DAYS_IN_WEEK - firstDayInWeek);
                }

                calendarItems.add(new CalendarHeaderItem(calendar.get(Calendar.MONTH), shift + daysEnded, shift + daysEnded));
                shift += 1;

                if (firstDayInWeek != 0) {
                    calendarItems.add(new CalendarEmptyItem(shift + daysEnded, shift + daysEnded + firstDayInWeek - 1));
                    shift += firstDayInWeek;
                }

            } else {
                calendarItems.add(new CalendarDayItem(firstRangeDate, firstDate + 1, shift + daysEnded, shift + totalDaysCount,
                        CalendarDateState.AFTER_TODAY));
                break;
            }
        }

        return calendarItems;
    }

    private static List<CalendarItem> fillCalendarTillCurrentDate(final Calendar cleanStartDate, final Calendar calendar) {
        final List<CalendarItem> calendarItems = new ArrayList<>();
        int shift = 0;
        int firstDate = calendar.get(Calendar.DAY_OF_MONTH) - 1;

        // add first month header
        calendarItems.add(new CalendarHeaderItem(calendar.get(Calendar.MONTH), 0, 0));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        shift += 1;

        final int firstDayInTheWeek = getFirstDateStart(calendar);

        // check if first day is Monday. If not - add empty items. Otherwise do nothing
        if (firstDayInTheWeek != 0) {
            calendarItems.add(new CalendarEmptyItem(shift, shift + firstDayInTheWeek - 1));
        }
        shift += firstDayInTheWeek;

        // add range with days before today
        calendarItems.add(new CalendarDayItem(calendar.getTimeInMillis() / CalendarAdapter.ONE_DAY_LENGTH + 1,
                1, shift, shift + firstDate - 1, CalendarDateState.BEFORE_TODAY));
        shift += firstDate - 1;

        // add today item
        calendar.setTime(cleanStartDate.getTime());
        calendarItems.add(new CalendarDayItem(calendar.getTimeInMillis() / CalendarAdapter.ONE_DAY_LENGTH + 1,
                firstDate+1, shift + 1, shift + 1, CalendarDateState.TODAY));

        return calendarItems;
    }

    private static int getFirstDateStart(@NonNull final Calendar calendar) {
        int firstDateStart = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (firstDateStart == -1) {
            firstDateStart += DAYS_IN_WEEK;
        }
        return firstDateStart;
    }


    public static boolean isToday(@NonNull final Date currentDate, @NonNull final Date date) {
        return currentDate.getTime() / CalendarAdapter.ONE_DAY_LENGTH == date.getTime() / CalendarAdapter.ONE_DAY_LENGTH;
    }


    @NonNull
    private static Calendar getCleanDate(@NonNull final Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    private CalendarUtils() {
    }

}
