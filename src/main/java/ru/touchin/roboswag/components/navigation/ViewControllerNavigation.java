package ru.touchin.roboswag.components.navigation;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.io.Serializable;

import ru.touchin.roboswag.components.utils.Logic;
import rx.functions.Func1;

/**
 * Created by Gavriil Sitnikov on 07/03/2016.
 * TODO: fill description
 */
public class ViewControllerNavigation<TLogic extends Logic> extends FragmentNavigation {

    @SuppressWarnings("CPD-START")
    public ViewControllerNavigation(@NonNull final ViewControllerActivity<TLogic> context,
                                    @NonNull final FragmentManager fragmentManager,
                                    @IdRes final int containerViewId) {
        super(context, fragmentManager, containerViewId);
    }

    public <TState extends Serializable> void push(@NonNull final Class<? extends ViewControllerFragment<TState, TLogic,
            ? extends ViewControllerActivity<TLogic>>> fragmentClass,
                                                   @NonNull final TState state) {
        addToStack(fragmentClass, null, ViewControllerFragment.createState(state), null, null);
    }

    public <TState extends Serializable> void push(@NonNull final Class<? extends ViewControllerFragment<TState, TLogic,
            ? extends ViewControllerActivity<TLogic>>> fragmentClass,
                                                   @Nullable final TState state,
                                                   @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, null, ViewControllerFragment.createState(state), null, transactionSetup);
    }

    public <TState extends Serializable> void pushForResult(@NonNull final Class<? extends ViewControllerFragment<TState, TLogic,
            ? extends ViewControllerActivity<TLogic>>> fragmentClass,
                                                            @NonNull final Fragment targetFragment,
                                                            @NonNull final TState state) {
        addToStack(fragmentClass, targetFragment, ViewControllerFragment.createState(state), fragmentClass.getName() + ';'
                + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    public <TState extends Serializable> void pushForResult(@NonNull final Class<? extends ViewControllerFragment<TState, TLogic,
            ? extends ViewControllerActivity<TLogic>>> fragmentClass,
                                                            @NonNull final Fragment targetFragment,
                                                            @Nullable final TState state,
                                                            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(fragmentClass, targetFragment, ViewControllerFragment.createState(state), fragmentClass.getName() + ';'
                + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public <TState extends Serializable> void setAsTop(@NonNull final Class<? extends ViewControllerFragment<TState, TLogic,
            ? extends ViewControllerActivity<TLogic>>> fragmentClass,
                                                       @NonNull final TState state) {
        setAsTop(fragmentClass, ViewControllerFragment.createState(state), null);
    }

    public <TState extends Serializable> void setAsTop(@NonNull final Class<? extends ViewControllerFragment<TState, TLogic,
            ? extends ViewControllerActivity<TLogic>>> fragmentClass,
                                                       @Nullable final TState state,
                                                       @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        setAsTop(fragmentClass, ViewControllerFragment.createState(state), transactionSetup);
    }

    public <TState extends Serializable> void setInitial(@NonNull final Class<? extends ViewControllerFragment<TState, TLogic,
            ? extends ViewControllerActivity<TLogic>>> fragmentClass,
                                                         @NonNull final TState state) {
        setInitial(fragmentClass, ViewControllerFragment.createState(state), null);
    }

    public <TState extends Serializable> void setInitial(@NonNull final Class<? extends ViewControllerFragment<TState, TLogic,
            ? extends ViewControllerActivity<TLogic>>> fragmentClass,
                                                         @Nullable final TState state,
                                                         @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        setInitial(fragmentClass, ViewControllerFragment.createState(state), transactionSetup);
    }

    public void pushViewController(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends StatelessViewControllerFragment<TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass) {
        addStatelessViewControllerToStack(viewControllerClass, null, null, null);
    }

    public <TState extends Serializable> void pushViewController(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends SimpleViewControllerFragment<TState, TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                                                 @NonNull final TState state) {
        addViewControllerToStack(viewControllerClass, null, state, null, null);
    }

    public <TState extends Serializable> void pushViewController(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends SimpleViewControllerFragment<TState, TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                                                 @Nullable final TState state,
                                                                 @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addViewControllerToStack(viewControllerClass, null, state, null, transactionSetup);
    }

    public void pushViewControllerForResult(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends StatelessViewControllerFragment<TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                            @NonNull final Fragment targetFragment) {
        addStatelessViewControllerToStack(viewControllerClass, targetFragment,
                viewControllerClass.getName() + ';' + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    public <TState extends Serializable> void pushViewControllerForResult(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends SimpleViewControllerFragment<TState, TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                                                          @NonNull final Fragment targetFragment,
                                                                          @NonNull final TState state) {
        addViewControllerToStack(viewControllerClass, targetFragment, state, viewControllerClass.getName() + ';'
                + WITH_TARGET_FRAGMENT_TAG_MARK, null);
    }

    public <TState extends Serializable> void pushViewControllerForResult(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends SimpleViewControllerFragment<TState, TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                                                          @NonNull final Fragment targetFragment,
                                                                          @Nullable final TState state,
                                                                          @Nullable final Func1<FragmentTransaction, FragmentTransaction>
                                                                                  transactionSetup) {
        addViewControllerToStack(viewControllerClass, targetFragment, state, viewControllerClass.getName() + ';'
                + WITH_TARGET_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void setViewControllerAsTop(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends StatelessViewControllerFragment<TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass) {
        addStatelessViewControllerToStack(viewControllerClass, null, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, null);
    }

    public <TState extends Serializable> void setViewControllerAsTop(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends SimpleViewControllerFragment<TState, TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                                                     @NonNull final TState state) {
        addViewControllerToStack(viewControllerClass, null, state, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, null);
    }

    public <TState extends Serializable> void setViewControllerAsTop(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends SimpleViewControllerFragment<TState, TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                                                     @Nullable final TState state,
                                                                     @Nullable final Func1<FragmentTransaction, FragmentTransaction>
                                                                             transactionSetup) {
        addViewControllerToStack(viewControllerClass, null, state, viewControllerClass.getName() + ' ' + TOP_FRAGMENT_TAG_MARK, transactionSetup);
    }

    public void setInitialViewController(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends StatelessViewControllerFragment<TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass) {
        beforeSetInitialActions();
        setViewControllerAsTop(viewControllerClass);
    }

    public <TState extends Serializable> void setInitialViewController(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends SimpleViewControllerFragment<TState, TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                                                       @NonNull final TState state) {
        setInitialViewController(viewControllerClass, state, null);
    }

    public <TState extends Serializable> void setInitialViewController(@NonNull final Class<? extends ViewController<TLogic,
            ? extends ViewControllerActivity<TLogic>,
            ? extends SimpleViewControllerFragment<TState, TLogic, ? extends ViewControllerActivity<TLogic>>>> viewControllerClass,
                                                                       @Nullable final TState state,
                                                                       @Nullable final Func1<FragmentTransaction, FragmentTransaction>
                                                                               transactionSetup) {
        beforeSetInitialActions();
        setViewControllerAsTop(viewControllerClass, state, transactionSetup);
    }

    protected <TState extends Serializable> void addViewControllerToStack(
            @NonNull final Class<? extends ViewController<TLogic, ? extends ViewControllerActivity<TLogic>, ?>> viewControllerClass,
            @Nullable final Fragment targetFragment,
            @Nullable final TState state,
            @Nullable final String backStackTag,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(SimpleViewControllerFragment.class, targetFragment,
                SimpleViewControllerFragment.createState(viewControllerClass, state), backStackTag, transactionSetup);
    }

    @SuppressWarnings("CPD-END")
    protected <TState extends Serializable> void addStatelessViewControllerToStack(
            @NonNull final Class<? extends ViewController<TLogic, ? extends ViewControllerActivity<TLogic>, ?>> viewControllerClass,
            @Nullable final Fragment targetFragment,
            @Nullable final String backStackTag,
            @Nullable final Func1<FragmentTransaction, FragmentTransaction> transactionSetup) {
        addToStack(StatelessViewControllerFragment.class, targetFragment,
                SimpleViewControllerFragment.createState(viewControllerClass, null), backStackTag, transactionSetup);
    }

}