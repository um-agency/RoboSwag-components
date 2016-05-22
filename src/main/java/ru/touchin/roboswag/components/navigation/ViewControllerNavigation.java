package ru.touchin.roboswag.components.navigation;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import ru.touchin.roboswag.components.navigation.activities.ViewControllerActivity;
import ru.touchin.roboswag.components.navigation.fragments.SimpleViewControllerFragment;
import ru.touchin.roboswag.components.navigation.fragments.StatelessTargetedViewControllerFragment;
import ru.touchin.roboswag.components.navigation.fragments.StatelessViewControllerFragment;
import ru.touchin.roboswag.components.navigation.fragments.TargetedViewControllerFragment;
import ru.touchin.roboswag.components.navigation.fragments.ViewControllerFragment;
import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * TODO: fill description
 */
public class ViewControllerNavigation<TActivity extends ViewControllerActivity<?>> extends FragmentNavigation {

    public ViewControllerNavigation(@NonNull final Context context,
                                    @NonNull final FragmentManager fragmentManager,
                                    @IdRes final int containerViewId) {
        super(context, fragmentManager, containerViewId);
    }

    public <TState extends AbstractState> void push(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                    @NonNull final TState state) {
        addToStack(fragmentClass, null, ViewControllerFragment.createState(state), null, null);
    }

    public <TState extends AbstractState> void push(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                    @Nullable final TState state,
                                                    @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, ViewControllerFragment.createState(state), null, transactionSetup);
    }

    public <TState extends AbstractState> void pushForResult(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                             @NonNull final Fragment targetFragment,
                                                             @NonNull final TState state) {
        addToStack(fragmentClass, targetFragment, ViewControllerFragment.createState(state),
                fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    public <TState extends AbstractState> void pushForResult(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                             @NonNull final Fragment targetFragment,
                                                             @Nullable final TState state,
                                                             @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, targetFragment, ViewControllerFragment.createState(state),
                fragmentClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public <TState extends AbstractState> void setAsTop(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                        @NonNull final TState state) {
        setAsTop(fragmentClass, ViewControllerFragment.createState(state), null);
    }

    public <TState extends AbstractState> void setAsTop(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                        @Nullable final TState state,
                                                        @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        setAsTop(fragmentClass, ViewControllerFragment.createState(state), transactionSetup);
    }

    public <TState extends AbstractState> void setInitial(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                          @NonNull final TState state) {
        setInitial(fragmentClass, ViewControllerFragment.createState(state), null);
    }

    public <TState extends AbstractState> void setInitial(@NonNull final Class<? extends ViewControllerFragment<TState, TActivity>> fragmentClass,
                                                          @Nullable final TState state,
                                                          @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        setInitial(fragmentClass, ViewControllerFragment.createState(state), transactionSetup);
    }

    public void pushViewController(@NonNull final Class<? extends ViewController<TActivity,
            StatelessViewControllerFragment<TActivity>>> viewControllerClass) {
        addStatelessViewControllerToStack(viewControllerClass, null, null, null);
    }

    public <TState extends AbstractState> void pushViewController(@NonNull final Class<? extends ViewController<TActivity,
            SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
                                                                  @NonNull final TState state) {
        addViewControllerToStack(viewControllerClass, null, state, null, null);
    }

    public <TState extends AbstractState> void pushViewController(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addViewControllerToStack(viewControllerClass, null, state, null, transactionSetup);
    }

    public void pushViewController(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addStatelessViewControllerToStack(viewControllerClass, null, null, transactionSetup);
    }

    public <TTargetState extends AbstractState,
            TTargetFragment extends ViewControllerFragment<TTargetState, TActivity>> void pushViewControllerForResult(
            @NonNull final Class<? extends ViewController<TActivity,
                    StatelessTargetedViewControllerFragment<TTargetState, TActivity>>> viewControllerClass,
            @NonNull final TTargetFragment targetFragment) {
        addTargetedStatelessViewControllerToStack(viewControllerClass, targetFragment,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    @SuppressWarnings("CPD-START")
    public <TState extends AbstractState, TTargetState extends AbstractState,
            TTargetFragment extends ViewControllerFragment<TTargetState, TActivity>> void pushViewControllerForResult(
            @NonNull final Class<? extends ViewController<TActivity,
                    TargetedViewControllerFragment<TState, TTargetState, TActivity>>> viewControllerClass,
            @NonNull final TTargetFragment targetFragment,
            @NonNull final TState state) {
        addTargetedViewControllerToStack(viewControllerClass, targetFragment, state,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    @SuppressWarnings("CPD-END")
    public <TState extends AbstractState, TTargetState extends AbstractState,
            TTargetFragment extends ViewControllerFragment<TTargetState, TActivity>> void pushViewControllerForResult(
            @NonNull final Class<? extends ViewController<TActivity,
                    TargetedViewControllerFragment<TState, TTargetState, TActivity>>> viewControllerClass,
            @NonNull final TTargetFragment targetFragment,
            @NonNull final TState state,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addTargetedViewControllerToStack(viewControllerClass, targetFragment, state,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public <TTargetState extends AbstractState,
            TTargetFragment extends ViewControllerFragment<TTargetState, TActivity>> void pushViewControllerForResult(
            @NonNull final Class<? extends ViewController<TActivity,
                    StatelessTargetedViewControllerFragment<TTargetState, TActivity>>> viewControllerClass,
            @NonNull final TTargetFragment targetFragment,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addTargetedStatelessViewControllerToStack(viewControllerClass, targetFragment,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void setViewControllerAsTop(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass) {
        addStatelessViewControllerToStack(viewControllerClass, null, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, null);
    }

    public <TState extends AbstractState> void setViewControllerAsTop(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state) {
        addViewControllerToStack(viewControllerClass, null, state, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, null);
    }

    public <TState extends AbstractState> void setViewControllerAsTop(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addViewControllerToStack(viewControllerClass, null, state, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void setViewControllerAsTop(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addStatelessViewControllerToStack(viewControllerClass, null, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void setInitialViewController(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass) {
        beforeSetInitialActions();
        setViewControllerAsTop(viewControllerClass);
    }

    public <TState extends AbstractState> void setInitialViewController(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state) {
        setInitialViewController(viewControllerClass, state, null);
    }

    public <TState extends AbstractState> void setInitialViewController(
            @NonNull final Class<? extends ViewController<TActivity, SimpleViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final TState state,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        beforeSetInitialActions();
        setViewControllerAsTop(viewControllerClass, state, transactionSetup);
    }

    public void setInitialViewController(
            @NonNull final Class<? extends ViewController<TActivity, StatelessViewControllerFragment<TActivity>>> viewControllerClass,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        beforeSetInitialActions();
        setViewControllerAsTop(viewControllerClass, transactionSetup);
    }

    protected <TState extends AbstractState> void addViewControllerToStack(
            @NonNull final Class<? extends ViewController<TActivity, ? extends ViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @Nullable final Fragment targetFragment,
            @NonNull final TState state,
            @Nullable final String backStackTag,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(SimpleViewControllerFragment.class, targetFragment,
                SimpleViewControllerFragment.createState(viewControllerClass, state), backStackTag, transactionSetup);
    }

    protected <TState extends AbstractState> void addTargetedViewControllerToStack(
            @NonNull final Class<? extends ViewController<TActivity, ? extends ViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final Fragment targetFragment,
            @NonNull final TState state,
            @Nullable final String backStackTag,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(TargetedViewControllerFragment.class, targetFragment,
                SimpleViewControllerFragment.createState(viewControllerClass, state), backStackTag, transactionSetup);
    }

    protected void addStatelessViewControllerToStack(
            @NonNull final Class<? extends ViewController<TActivity, ? extends ViewControllerFragment<AbstractState, TActivity>>> viewControllerClass,
            @Nullable final Fragment targetFragment,
            @Nullable final String backStackTag,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(StatelessViewControllerFragment.class, targetFragment,
                StatelessViewControllerFragment.createState(viewControllerClass, null), backStackTag, transactionSetup);
    }

    protected <TState extends AbstractState> void addTargetedStatelessViewControllerToStack(
            @NonNull final Class<? extends ViewController<TActivity, ? extends ViewControllerFragment<TState, TActivity>>> viewControllerClass,
            @NonNull final Fragment targetFragment,
            @Nullable final String backStackTag,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(StatelessTargetedViewControllerFragment.class, targetFragment,
                StatelessTargetedViewControllerFragment.createState(viewControllerClass, null), backStackTag, transactionSetup);
    }

}