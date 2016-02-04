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

package org.roboswag.components.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;

import org.roboswag.components.services.LogicService;
import org.roboswag.core.log.Lc;
import org.roboswag.core.utils.android.RxAndroidUtils;

import rx.Observable;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 21/10/2015.
 * TODO: fill description
 */
public abstract class ViewControllerFragment<TLogicBridge, TActivity extends AppCompatActivity> extends ViewFragment<TActivity> {

    private final BehaviorSubject<TActivity> activitySubject = BehaviorSubject.create();
    private final BehaviorSubject<Pair<View, Bundle>> viewSubject = BehaviorSubject.create();
    @Nullable
    private ViewController viewController;
    @Nullable
    private Subscription viewControllerSubscription;

    /**
     * It should return specific Service class where from this fragment should get interface to logic.
     *
     * @return Returns class of specific LogicService.
     */
    @NonNull
    protected abstract Class<LogicService<TLogicBridge>> getLogicServiceClass();

    /**
     * It should return specific ViewController class to control instantiated view by logic bridge after activity creation.
     *
     * @return Returns class of specific ViewController.
     */
    @NonNull
    protected abstract Class<ViewController<TLogicBridge, TActivity,
            ? extends ViewControllerFragment<TLogicBridge, TActivity>>> getViewControllerClass();

    // need throwable for app stability
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getContext() != null) {
            viewControllerSubscription = Observable.combineLatest(activitySubject.distinctUntilChanged(), viewSubject.distinctUntilChanged(),
                    RxAndroidUtils.observeService(getContext(), getLogicServiceClass())
                            .map(service -> service != null ? service.getLogicBridge() : null)
                            .distinctUntilChanged(),
                    (activity, view, logicBridge) -> {
                        if (activity == null || view == null || logicBridge == null) {
                            return null;
                        }

                        final ViewController.CreationContext<TLogicBridge, TActivity,
                                ? extends ViewControllerFragment<TLogicBridge, TActivity>> creationContext
                                = new ViewController.CreationContext<>(logicBridge, activity, this, view.first);
                        if (getViewControllerClass().getConstructors().length == 1) {
                            try {
                                return (ViewController) getViewControllerClass().getConstructors()[0].newInstance(creationContext, view.second);
                            } catch (Throwable throwable) {
                                Lc.assertion(throwable);
                            }
                        } else {
                            Lc.assertion("There should be single constructor for " + getViewControllerClass());
                        }
                        return null;
                    }).subscribe(this::onViewControllerChanged);
        } else {
            Lc.assertion("Context is null in onCreate.");
        }
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO...
    }

    @Override
    public void onActivityCreated(@NonNull final View view, @NonNull final TActivity activity, @Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(view, activity, savedInstanceState);
        activitySubject.onNext(activity);
    }

    private void onViewControllerChanged(@Nullable final ViewController viewController) {
        if (this.viewController != null) {
            this.viewController.onDestroy();
        }
        this.viewController = viewController;
    }

    @Override
    protected void onDestroyView(@NonNull final View view) {
        viewSubject.onNext(null);
        super.onDestroyView(view);
    }

    @Override
    public void onDestroy() {
        if (viewControllerSubscription != null) {
            viewControllerSubscription.unsubscribe();
            viewControllerSubscription = null;
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        activitySubject.onNext(null);
        super.onDetach();
    }

}
