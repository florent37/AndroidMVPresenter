package com.github.florent37.androidmvpresenter.sample;

import io.reactivex.Single;

public interface AuthRepo {
    Single<Boolean> authentificate();
}
