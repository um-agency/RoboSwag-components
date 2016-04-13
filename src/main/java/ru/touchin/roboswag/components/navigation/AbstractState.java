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

package ru.touchin.roboswag.components.navigation;

import java.io.Serializable;

/**
 * Created by Ilia Kurtov on 13.04.2016.
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
//AbstractClassWithoutAbstractMethod: objects of this class actually shouldn't exist
public abstract class AbstractState implements Serializable {

    private static final long serialVersionUID = 1L;

    public void onCreate(){
        // do nothing
    }

}
