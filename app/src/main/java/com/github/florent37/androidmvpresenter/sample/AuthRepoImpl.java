package com.github.florent37.androidmvpresenter.sample;

import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.functions.Function;

public class AuthRepoImpl implements AuthRepo  {

    public Single<Boolean> authentificate(){
        return Single.timer(3, TimeUnit.SECONDS)
                .map(new Function<Long, Boolean>() {
                    @Override
                    public Boolean apply(Long aLong) throws Exception {
                        return true;
                    }
                });
    }
}
