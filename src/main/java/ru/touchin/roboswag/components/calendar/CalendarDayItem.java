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

/**
 * Created by Ilia Kurtov on 17.03.2016.
 * * //TODO: fill description
 */
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