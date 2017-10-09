package com.github.florent37.androidmvpresenter;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

public class RxClick {
    public static Observable<View> with(final View view) {
        return Observable.create(new ObservableOnSubscribe<View>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<View> e) throws Exception {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View value) {
                                e.onNext(value);
                            }
                        });
                    }
                });
            }
        });
    }
}
