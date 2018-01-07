package com.github.florent37.androidmvpresenter.sample;


import com.github.florent37.androidmvpresenter.AbstractPresenter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

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
                        if (throwable instanceof AuthentificationException && ((AuthentificationException) throwable).statusCode == 401) {
                            return authRepo.authentificate().toObservable();
                        } else if (throwable instanceof IOException) {
                            return Observable.timer(3, TimeUnit.SECONDS); //wait 3 seconds before continue
                        }
                        return Observable.error(throwable);
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

    public interface View extends AbstractPresenter.View {
        void sayHello();
    }

    //example of Auth Error
    private class AuthentificationException extends Throwable {
        private int statusCode;
    }
}
