package com.github.florent37.androidmvpresenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.CallSuper;

import org.reactivestreams.Publisher;

import java.lang.ref.WeakReference;

import florent37.github.com.rxlifecycle.RxLifecycle;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by florentchampigny on 08/08/2017.
 */

public abstract class AbstractPresenter<V extends AbstractPresenter.View> {

    private final CompositeDisposable compositeDisposable;
    private int maxRetry = 3;
    private WeakReference<V> viewReference;
    private Function<Throwable, Observable<?>> todoBeforeRetry;

    public AbstractPresenter() {
        compositeDisposable = new CompositeDisposable();
    }

    public void setupRetry(int maxRetry, Function<Throwable, Observable<?>> todoBeforeRetry){
        this.maxRetry = maxRetry;
        this.todoBeforeRetry = todoBeforeRetry;
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

    protected static class RetryWithDelay {

        private final int maxRetries;
        private int retryCount = 0;
        private Function<Throwable, Observable<?>> todoBeforeRetry;

        public RetryWithDelay(final int maxRetries, final Function<Throwable, Observable<?>> todoBeforeRetry) {
            this.maxRetries = maxRetries;
            this.todoBeforeRetry = todoBeforeRetry;
        }

        public Function<Observable<? extends Throwable>, Observable<?>> forObservable = new Function<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> apply(Observable<? extends Throwable> observable) throws Exception {
                return observable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Exception {
                        if(++retryCount <= maxRetries){
                            return todoBeforeRetry.apply(throwable);
                        }
                        return Observable.error(throwable);
                    }
                });
            }
        };

        public Function<Flowable<Throwable>, Publisher<?>> forSingle = new Function<Flowable<Throwable>, Publisher<?>>() {
            @Override
            public Publisher<?> apply(Flowable<Throwable> throwableFlowable) throws Exception {
                return throwableFlowable.flatMap(new Function<Throwable, Publisher<?>>() {
                    @Override
                    public Publisher<?> apply(Throwable throwable) throws Exception {
                        if(++retryCount <= maxRetries){
                            return todoBeforeRetry.apply(throwable).toFlowable(BackpressureStrategy.BUFFER);
                        }
                        return Flowable.error(throwable);
                    }
                });
            }
        };
    }

    public <R> SingleTransformer<? super R, ? extends R> composeSingle() {
        return new SingleTransformer<R, R>() {
            @Override
            public SingleSource<R> apply(@NonNull Single<R> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(new RetryWithDelay(maxRetry, todoBeforeRetry).forSingle)
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(@NonNull Disposable disposable) throws Exception {
                                AbstractPresenter.this.addDisposable(disposable);
                            }
                        });
            }
        };
    }

    public <R> ObservableTransformer<? super R, ? extends R> composeObservable() {
        return new ObservableTransformer<R, R>() {
            @Override
            public ObservableSource<R> apply(@NonNull Observable<R> upstream) {
                return upstream
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(new RetryWithDelay(maxRetry, todoBeforeRetry).forObservable)
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
