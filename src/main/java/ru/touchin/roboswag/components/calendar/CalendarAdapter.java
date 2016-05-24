

package ru.touchin.roboswag.components.calendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ilia Kurtov on 17.03.2016.
 * * //TODO: fill description
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.GodClass"})
public abstract class CalendarAdapter<TDayViewHolder extends RecyclerView.ViewHolder, THeaderViewHolder extends RecyclerView.ViewHolder,
        TEmptyViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int HEADER_ITEM_TYPE = 0;
    public static final int EMPTY_ITEM_TYPE = 1;
    public static final int DAY_ITEM_TYPE = 2;

    public static final int MONTHS_IN_YEAR = 12;
    public static final long ONE_DAY_LENGTH = TimeUnit.DAYS.toMillis(1);

    private List<CalendarItem> calendarItems;

    @Nullable
    private Integer startSelectionPosition;
    @Nullable
    private Integer endSelectionPosition;

    @NonNull
    private final Context context;
    private String[] monthsNames;

    public enum State {
        SELECTED_FIRST,
        SELECTED_MIDDLE,
        SELECTED_LAST,
        SELECTED_ONE_ONLY,
        NOT_SELECTED
    }

    protected CalendarAdapter(@NonNull final Context context, @Nullable final String... monthsNames) {
        super();
        this.context = context;
        if (monthsNames != null && monthsNames.length == MONTHS_IN_YEAR) {
            this.monthsNames = monthsNames;
        }
    }

    public void setRange(@NonNull final Calendar startDate, @NonNull final Calendar endDate) {
        calendarItems = CalendarUtils.fillRanges(startDate, endDate);
    }

    @NonNull
    protected Context getContext() {
        return context;
    }

    public void setSelectedRange(@Nullable final Date startSelectionDate, @Nullable final Date endSelectionDate) {
        if (startSelectionDate != null) {
            startSelectionPosition = CalendarUtils.findPositionByDate(calendarItems, startSelectionDate.getTime() / ONE_DAY_LENGTH);
        }
        if (endSelectionDate != null) {
            endSelectionPosition = CalendarUtils.findPositionByDate(calendarItems, endSelectionDate.getTime() / ONE_DAY_LENGTH);
        }

        notifySelectedDaysChanged();
    }

    @Nullable
    public Integer getPositionToScroll(final boolean isDeparture) {
        if (isDeparture && startSelectionPosition != null) {
            return CalendarUtils.findPositionOfSelectedMonth(calendarItems, startSelectionPosition);
        }
        if (!isDeparture && endSelectionPosition != null) {
            return CalendarUtils.findPositionOfSelectedMonth(calendarItems, endSelectionPosition);
        }
        if (!isDeparture && startSelectionPosition != null) {
            return CalendarUtils.findPositionOfSelectedMonth(calendarItems, startSelectionPosition);
        }
        return null;
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

    protected abstract void bindHeaderItem(@NonNull final THeaderViewHolder viewHolder, @NonNull final String monthName, final boolean isFirstMonth);

    protected abstract void bindEmptyItem(@NonNull final TEmptyViewHolder viewHolder, @NonNull final CalendarAdapter.State state);

    protected abstract void bindDayItem(@NonNull final TDayViewHolder viewHolder,
                                        @NonNull final String day,
                                        @NonNull final Date date,
                                        @NonNull final CalendarAdapter.State state,
                                        @NonNull final CalendarDateState dateState);

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final CalendarItem calendarItem = CalendarUtils.findItemByPosition(calendarItems, position);

        if (calendarItem instanceof CalendarHeaderItem) {
            final StaggeredGridLayoutManager.LayoutParams layoutParams =
                    new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setFullSpan(true);
            holder.itemView.setLayoutParams(layoutParams);
            final String monthName;
            monthName = monthsNames != null ? monthsNames[((CalendarHeaderItem) calendarItem).getMonth()]
                    : String.valueOf(((CalendarHeaderItem) calendarItem).getMonth());
            bindHeaderItem((THeaderViewHolder) holder, monthName, position == 0);
        } else if (calendarItem instanceof CalendarEmptyItem) {
            if (startSelectionPosition != null && endSelectionPosition != null
                    && position >= startSelectionPosition && position <= endSelectionPosition) {
                bindEmptyItem((TEmptyViewHolder) holder, State.SELECTED_MIDDLE);
            } else {
                bindEmptyItem((TEmptyViewHolder) holder, State.NOT_SELECTED);
            }
        } else if (calendarItem instanceof CalendarDayItem) {
            bindDay((TDayViewHolder) holder, position, calendarItem);
        }
    }

    private void bindDay(final TDayViewHolder holder, final int position, final CalendarItem calendarItem) {
        final String currentDay = String.valueOf(((CalendarDayItem) calendarItem).getPositionOfFirstDay()
                + position - calendarItem.getStartRange());
        final Date currentDate = new Date((((CalendarDayItem) calendarItem).getDateOfFirstDay()
                + position - calendarItem.getStartRange()) * ONE_DAY_LENGTH);
        final CalendarDateState dateState = ((CalendarDayItem) calendarItem).getDateState();
        if (startSelectionPosition != null && position == startSelectionPosition) {
            if (endSelectionPosition == null || endSelectionPosition.equals(startSelectionPosition)) {
                bindDayItem(holder, currentDay, currentDate, State.SELECTED_ONE_ONLY, dateState);
                return;
            }
            bindDayItem(holder, currentDay, currentDate, State.SELECTED_FIRST, dateState);
            return;
        }
        if (endSelectionPosition != null && position == endSelectionPosition) {
            bindDayItem(holder, currentDay, currentDate, State.SELECTED_LAST, dateState);
            return;
        }
        if (startSelectionPosition != null && endSelectionPosition != null
                && position >= startSelectionPosition && position <= endSelectionPosition) {
            bindDayItem(holder, currentDay, currentDate, State.SELECTED_MIDDLE, dateState);
            return;
        }

        bindDayItem(holder, currentDay, currentDate, State.NOT_SELECTED, dateState);
    }

    @Override
    public int getItemViewType(final int position) {
        final CalendarItem calendarItem = CalendarUtils.findItemByPosition(calendarItems, position);

        if (calendarItem instanceof CalendarHeaderItem) {
            return HEADER_ITEM_TYPE;
        } else if (calendarItem instanceof CalendarEmptyItem) {
            return EMPTY_ITEM_TYPE;
        } else if (calendarItem instanceof CalendarDayItem) {
            return DAY_ITEM_TYPE;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return calendarItems.isEmpty() ? 0 : calendarItems.get(calendarItems.size() - 1).getEndRange();
    }

}