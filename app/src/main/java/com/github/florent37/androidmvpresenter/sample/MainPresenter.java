package com.github.florent37.androidmvpresenter.sample;


import com.github.florent37.androidmvpresenter.AbstractPresenter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class MainPresenter extends AbstractPresenter<MainPresenter.View> {

    private final AuthRepo authRepo;

    public MainPresenter(final AuthRepo authRepo) {
        super();
        this.authRepo = authRepo;

        super.setupRetry(
                3,
                new Function<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Throwable throwable) throws Exception {
                        if(throwable instanceof AuthentificationException && ((AuthentificationException) throwable).statusCode == 401){
                            return authRepo.authentificate().toObservable();
                        } else if(throwable instanceof IOException) {
                            return Observable.timer(3, TimeUnit.SECONDS); //wait 3 seconds before continue
                        }
                    }
                });
    }

    @Override
    protected void start() {
        onView(new AbstractPresenter.ViewCallback<View>() {
            @Override
            public void onView(View view) {
                view.sayHello();
            }
        });
    }

    //example of Auth Error
    private class AuthentificationException extends Throwable {
        private int statusCode;
    }

    public interface View extends AbstractPresenter.View {
        void sayHello();
    }
}
