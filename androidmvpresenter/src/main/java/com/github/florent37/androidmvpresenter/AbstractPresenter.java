package com.github.florent37.androidmvpresenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.CallSuper;

import java.lang.ref.WeakReference;

import florent37.github.com.rxlifecycle.RxLifecycle;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by florentchampigny on 08/08/2017.
 */

public abstract class AbstractPresenter<V extends AbstractPresenter.View> {

    private final CompositeDisposable compositeDisposable;
    private WeakReference<V> viewReference;

    public AbstractPresenter() {
        compositeDisposable = new CompositeDisposable();
    }

    public void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    protected V getView() {
        return viewReference.get();
    }

    public void onView(ViewCallback<V> viewCallback) {
        if (viewCallback != null) {
            if (getView() != null) {
                viewCallback.onView(getView());
            }
        }
    }

    @CallSuper
    public void bind(LifecycleOwner lifecycleOwner, V view) {
        unbind();
        this.viewReference = new WeakReference<V>(view);
        RxLifecycle.with(lifecycleOwner)
                .onDestroy()
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        AbstractPresenter.this.addDisposable(disposable);
                    }
                })
                .subscribe(new Consumer<Lifecycle.Event>() {
                    @Override
                    public void accept(@NonNull Lifecycle.Event x) throws Exception {
                        AbstractPresenter.this.unbind();
                    }
                });

        RxLifecycle.with(lifecycleOwner)
                .onStart()
                .distinct() //once
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        AbstractPresenter.this.addDisposable(disposable);
                    }
                })
                .subscribe(new Consumer<Lifecycle.Event>() {
                    @Override
                    public void accept(@NonNull Lifecycle.Event x) throws Exception {
                        AbstractPresenter.this.start();
                    }
                });
    }

    protected abstract void start();

    @CallSuper
    protected void unbind() {
        compositeDisposable.clear();
        if (viewReference != null) {
            viewReference.clear();
        }
    }

    public <R> SingleTransformer<? super R, ? extends R> compose() {
        return new SingleTransformer<R, R>() {
            @Override
            public SingleSource<R> apply(@NonNull Single<R> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                AbstractPresenter.this.addDisposable(disposable);
                            }
                        });
            }
        };
    }

    public interface ViewCallback<V> {
        void onView(V view);
    }

    public interface View {

    }

}
