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

package ru.touchin.roboswag.components.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

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
public abstract class CalendarAdapter<TDayViewHolder extends RecyclerView.ViewHolder, THeaderViewHolder extends RecyclerView.ViewHolder,
        TEmptyViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_ITEM_TYPE = 0;
    private static final int EMPTY_ITEM_TYPE = 1;
    private static final int DAY_ITEM_TYPE = 2;

    public static final int DAYS_IN_WEEK = 7;
    public static final int MONTHS_IN_YEAR = 12;

    public static final long ONE_DAY_LENGTH = TimeUnit.DAYS.toMillis(1);

    private List<CalendarItem> calendarItems;

    @Nullable
    private Integer startSelectionPosition;
    @Nullable
    private Integer endSelectionPosition;

    @NonNull
    private final Context context;
    private String[] monthsNames = null;

    protected CalendarAdapter(@NonNull final Context context, @Nullable final String... monthsNames) {
        this.context = context;
        if (monthsNames != null && monthsNames.length == MONTHS_IN_YEAR) {
            this.monthsNames = monthsNames;
        }
    }

    public void setRange(@NonNull final Calendar startDate, @NonNull final Calendar endDate) {
        fillRanges(getCleanDate(startDate), getCleanDate(endDate));
        getItemCount();
    }

    @NonNull
    protected Context getContext() {
        return context;
    }

    public void setSelectedRange(@Nullable final Date startSelectionDate, @Nullable final Date endSelectionDate) {
        if (startSelectionDate != null) {
            startSelectionPosition = findPositionByDate(startSelectionDate.getTime() / ONE_DAY_LENGTH);
        }
        if (endSelectionDate != null) {
            endSelectionPosition = findPositionByDate(endSelectionDate.getTime() / ONE_DAY_LENGTH);
        }

        notifySelectedDaysChanged();
    }

    private void notifySelectedDaysChanged() {
        if (startSelectionPosition == null && endSelectionPosition == null) {
            return;
        }
        if (startSelectionPosition == null) {
            notifyItemRangeChanged(endSelectionPosition, 1);
            return;
        }
        if (endSelectionPosition == null) {
            notifyItemRangeChanged(startSelectionPosition, 1);
            return;
        }
        notifyItemRangeChanged(startSelectionPosition, endSelectionPosition - startSelectionPosition);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case HEADER_ITEM_TYPE:
                return createHeaderViewHolder(parent);
            case EMPTY_ITEM_TYPE:
                return createEmptyViewHolder(parent);
            case DAY_ITEM_TYPE:
                return createDayViewHolder(parent);
            default:
                return null;
        }
    }

    protected abstract THeaderViewHolder createHeaderViewHolder(final ViewGroup parent);

    protected abstract TEmptyViewHolder createEmptyViewHolder(final ViewGroup parent);

    protected abstract TDayViewHolder createDayViewHolder(final ViewGroup parent);

    protected abstract void bindHeaderItem(@NonNull final THeaderViewHolder viewHolder, @NonNull final String monthName);

    protected abstract void bindEmptyItem(@NonNull final TEmptyViewHolder viewHolder, @NonNull final CalendarState state);

    protected abstract void bindDayItem(@NonNull final TDayViewHolder viewHolder,
                                        @NonNull final String day,
                                        @NonNull final Date date,
                                        @NonNull final CalendarState state);

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final CalendarItem calendarItem = find(position);

        if (calendarItem instanceof CalendarHeaderItem) {
            final StaggeredGridLayoutManager.LayoutParams layoutParams =
                    new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setFullSpan(true);
            holder.itemView.setLayoutParams(layoutParams);
            final String monthName;
            if (monthsNames != null) {
                monthName = monthsNames[((CalendarHeaderItem) calendarItem).getMonth()];
            } else {
                monthName = String.valueOf(((CalendarHeaderItem) calendarItem).getMonth());
            }
            bindHeaderItem((THeaderViewHolder) holder, monthName);
        } else if (calendarItem instanceof CalendarEmptyItem) {
            if (startSelectionPosition != null && endSelectionPosition != null
                    && position >= startSelectionPosition && position <= endSelectionPosition) {
                bindEmptyItem((TEmptyViewHolder) holder, CalendarState.SELECTED_MIDDLE);
            } else {
                bindEmptyItem((TEmptyViewHolder) holder, CalendarState.NOT_SELECTED);
            }
        } else if (calendarItem instanceof CalendarDayItem) {
            final String currentDay = String.valueOf(((CalendarDayItem) calendarItem).getPositionOfFirstDay()
                    + position - calendarItem.getStartRange());
            final Date currentDate = new Date((((CalendarDayItem) calendarItem).getDateOfFirstDay() + position - calendarItem.getStartRange()) * ONE_DAY_LENGTH);
            if (startSelectionPosition != null && position == startSelectionPosition) {
                if (endSelectionPosition == null || endSelectionPosition.equals(startSelectionPosition)) {
                    bindDayItem((TDayViewHolder) holder, currentDay, currentDate, CalendarState.SELECTED_ONE_ONLY);
                    return;
                }
                bindDayItem((TDayViewHolder) holder, currentDay, currentDate, CalendarState.SELECTED_FIRST);
                return;
            }
            if (endSelectionPosition != null && position == endSelectionPosition) {
                bindDayItem((TDayViewHolder) holder, currentDay, currentDate, CalendarState.SELECTED_LAST);
                return;
            }
            if (startSelectionPosition != null && endSelectionPosition != null && position >= startSelectionPosition && position <= endSelectionPosition) {
                bindDayItem((TDayViewHolder) holder, currentDay, currentDate, CalendarState.SELECTED_MIDDLE);
                return;
            }

            bindDayItem((TDayViewHolder) holder, currentDay, currentDate, CalendarState.NOT_SELECTED);
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

    @Nullable
    public Integer findPositionByDate(final long date) {
        if (calendarItems != null) {
            int low = 0;
            int high = calendarItems.size() - 1;
            int addition = 0;
            float count = 0;
            while (true) {
                final int mid = (low + high) / 2 + addition;
                if (calendarItems.get(mid) instanceof CalendarDayItem) {
                    if (date < ((CalendarDayItem) calendarItems.get(mid)).getDateOfFirstDay()) {
                        if (mid == 0) {
                            break;
                        }
                        high = mid - 1;
                    } else {
                        final long endDate = ((CalendarDayItem) calendarItems.get(mid)).getDateOfFirstDay() +
                                calendarItems.get(mid).getEndRange() - calendarItems.get(mid).getStartRange();
                        if (date > endDate) {
                            if (mid == calendarItems.size()) {
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
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return calendarItems.isEmpty() ? 0 : calendarItems.get(calendarItems.size() - 1).getEndRange();
    }

    private void fillRanges(@NonNull final Calendar startDate, @NonNull final Calendar endDate) {
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
                calendarItems.add(new CalendarDayItem(firstRangeDate, firstRange + 1,
                        shift + daysEnded, shift + daysEnded + (daysInCurrentMonth - firstRange) - 1));
                daysEnded += daysInCurrentMonth - firstRange;
                if (daysEnded == totalDaysCount) {
                    return;
                }
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
                calendarItems.add(new CalendarDayItem(firstRangeDate, firstRange + 1, shift + daysEnded, shift + totalDaysCount));
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

    protected boolean isToday(@NonNull final Date currentDate, @NonNull final Date date) {
        return currentDate.getTime() / ONE_DAY_LENGTH == date.getTime() / ONE_DAY_LENGTH;
    }

    public static class CalendarDayItem implements CalendarItem {

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

    public enum CalendarState {
        SELECTED_FIRST,
        SELECTED_MIDDLE,
        SELECTED_LAST,
        SELECTED_ONE_ONLY,
        NOT_SELECTED
    }

}