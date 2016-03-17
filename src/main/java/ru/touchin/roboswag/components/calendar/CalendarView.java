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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import ru.touchin.roboswag.core.log.Lc;

/**
 * Created by Ilia Kurtov on 17.03.2016.
 * * //TODO: fill description
 */
public class CalendarView extends RecyclerView {

    private static final int HEADER_MAX_ELEMENTS_IN_A_ROW = 1;
    private static final int EMPTY_MAX_ELEMENTS_IN_A_ROW = 6;
    private static final int DAY_MAX_ELEMENTS_IN_A_ROW = 7;

    public CalendarView(@NonNull final Context context) {
        this(context, null);
    }

    public CalendarView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        getRecycledViewPool().setMaxRecycledViews(CalendarAdapter.HEADER_ITEM_TYPE, HEADER_MAX_ELEMENTS_IN_A_ROW + 1);
        getRecycledViewPool().setMaxRecycledViews(CalendarAdapter.EMPTY_ITEM_TYPE, EMPTY_MAX_ELEMENTS_IN_A_ROW + 1);
        getRecycledViewPool().setMaxRecycledViews(CalendarAdapter.DAY_ITEM_TYPE, DAY_MAX_ELEMENTS_IN_A_ROW + 1);
        setItemViewCacheSize(0);
    }

    @SuppressWarnings("PMD.UselessOverridingMethod")
    public void setAdapter(final CalendarAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    @Deprecated
    public void setAdapter(final Adapter adapter) {
        super.setAdapter(adapter);
        Lc.assertion("Unsupported adapter class. Use CalendarAdapter instead.");
    }

}