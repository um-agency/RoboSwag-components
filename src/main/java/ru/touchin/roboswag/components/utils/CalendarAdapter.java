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

package org.roboswag.components.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ilia Kurtov on 11.03.2016.
 * //TODO: fill description
 */
public class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_ITEM_TYPE = 0;
    private static final int EMPTY_ITEM_TYPE = 1;
    private static final int DAY_ITEM_TYPE = 2;

    public static final int DAYS_IN_WEEK = 7;
    public static final long ONE_WEEK_LENGTH = TimeUnit.DAYS.toMillis(7);
    public static final long ONE_DAY_LENGTH = TimeUnit.DAYS.toMillis(1);
    private int shift;
    private int emptyShift;
    private boolean isWeekShifted;
    private boolean isMonthStarted;
    private List<CalendarItem> calendarItems;

    private Calendar startDate;
    private Calendar endDate;

    private final Context context;

    public void setRange(@NonNull final Calendar startDate, @NonNull final Calendar endDate) {
        this.startDate = getCleanDate(startDate);
        this.endDate = getCleanDate(endDate);

        fillRanges();
        getItemCount();
    }

    public CalendarAdapter(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final TextView view = new TextView(context);
        final RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(100, 100);
        view.setLayoutParams(layoutParams);
        view.setGravity(Gravity.CENTER);

        switch (viewType) {
            case HEADER_ITEM_TYPE:
                return new HeaderViewHolder(view);
            case EMPTY_ITEM_TYPE:
                return new EmptyViewHolder(view);
            case DAY_ITEM_TYPE:
                return new DayViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final CalendarItem calendarItem = find(position);

        if (calendarItem instanceof CalendarHeaderItem) {
            final StaggeredGridLayoutManager.LayoutParams layoutParams =
                    new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setFullSpan(true);
            holder.itemView.setLayoutParams(layoutParams);
            ((HeaderViewHolder) holder).bindItem(((CalendarHeaderItem) calendarItem).getMonth());
        } else if (calendarItem instanceof CalendarEmptyItem) {
            ((EmptyViewHolder) holder).bindItem();
        } else if (calendarItem instanceof CalendarDayItem) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date((((CalendarDayItem) calendarItem).getFirstDayReal() + (position - calendarItem.getStartRange())) * ONE_DAY_LENGTH));
            ((DayViewHolder) holder).bindItem(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        }

    }

    @Override
    public int getItemViewType(final int position) {
        final CalendarItem calendarItem = find(position);
        if (calendarItem instanceof CalendarHeaderItem) {
            return HEADER_ITEM_TYPE;
        } else if (calendarItem instanceof CalendarEmptyItem) {
            return EMPTY_ITEM_TYPE;
        } else if (calendarItem instanceof CalendarDayItem) {
            return DAY_ITEM_TYPE;
        }

        return super.getItemViewType(position);
    }

    @Nullable
    public CalendarItem find(final long position) {
        if (calendarItems != null) {
            int low = 0;
            int high = calendarItems.size() - 1;
            while (true) {
                final int mid = (low + high) / 2;
                if (position < calendarItems.get(mid).getStartRange()) {
                    if (mid == 0 || position > calendarItems.get(mid - 1).getEndRange()) {
                        break;
                    }
                    high = mid - 1;
                } else if (position > calendarItems.get(mid).getEndRange()) {
                    if (mid == calendarItems.size() || position < calendarItems.get(mid + 1).getStartRange()) {
                        break;
                    }
                    low = mid + 1;
                } else {
                    return calendarItems.get(mid);
                }
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return calendarItems.isEmpty() ? 0 : calendarItems.get(calendarItems.size() - 1).getEndRange();
    }

    private void fillRanges() {
        calendarItems = new ArrayList<>();

        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(startDate.getTime());

        calendarItems.add(new CalendarHeaderItem(calendar.get(Calendar.MONTH), 0, 0));
        int shift = 1;

        final int totalDaysCount = (int) ((endDate.getTimeInMillis() - startDate.getTimeInMillis()) / ONE_DAY_LENGTH + 1);
        long firstRangeDate = calendar.getTimeInMillis() / ONE_DAY_LENGTH + 1;
        int firstRange = calendar.get(Calendar.DAY_OF_MONTH) - 1;
        int daysEnded = 0;

        shift += getFirstDateStart(startDate);
        if (shift > 1) {
            calendarItems.add(new CalendarEmptyItem(1, shift - 1));
        }

        while (true) {
            final int daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if ((daysEnded + (daysInCurrentMonth - firstRange)) <= totalDaysCount) {
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.setTime(new Date(calendar.getTimeInMillis() + ONE_DAY_LENGTH));

                final int firstDayInWeek = getFirstDateStart(calendar);
                calendarItems.add(new CalendarDayItem(firstRangeDate, shift + daysEnded, shift + daysEnded + (daysInCurrentMonth - firstRange) - 1));
                daysEnded += daysInCurrentMonth - firstRange;
                firstRangeDate = calendar.getTimeInMillis() / ONE_DAY_LENGTH + 1;

                firstRange = 0;

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
                calendarItems.add(new CalendarDayItem(firstRangeDate, shift + daysEnded, shift + totalDaysCount));
                break;
            }
        }
    }

    private static int getFirstDateStart(@NonNull final Calendar calendar) {
        int firstDateStart = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (firstDateStart == -1) {
            firstDateStart += DAYS_IN_WEEK;
        }
        return firstDateStart;
    }

    @NonNull
    private static Calendar getCleanDate(@NonNull final Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {

        private final TextView dayText;

        public DayViewHolder(final View itemView) {
            super(itemView);
            dayText = (TextView) itemView;
        }

        public void bindItem(@NonNull final String day) {
            dayText.setText(day);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        private final TextView dayText;

        public EmptyViewHolder(final View itemView) {
            super(itemView);
            dayText = (TextView) itemView;
        }

        public void bindItem() {
            dayText.setText(null);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView dayText;

        public HeaderViewHolder(final View itemView) {
            super(itemView);
            dayText = (TextView) itemView;
        }

        public void bindItem(final int monthName) {
            dayText.setText(String.valueOf(monthName));
        }

    }

    public static class CalendarDayItem implements CalendarItem {

        private final long firstDayReal;
        private final int startRange;
        private final int endRange;

        public CalendarDayItem(final long firstDayReal, final int startRange, final int endRange) {
            this.firstDayReal = firstDayReal;
            this.startRange = startRange;
            this.endRange = endRange;
        }

        public long getFirstDayReal() {
            return firstDayReal;
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

    public static class CalendarEmptyItem implements CalendarItem {

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

    public static class CalendarHeaderItem implements CalendarItem {

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

    private interface CalendarItem {
        int getStartRange();

        int getEndRange();
    }

}