package com.github.florent37.androidmvpresenter.sample;

import com.github.florent37.androidmvpresenter.AbstractPresenter;


public class MainPresenter extends AbstractPresenter<MainPresenter.View> {

    @Override
    protected void start() {
        onView(new ViewCallback<View>() {
            @Override
            public void onView(View view) {
                view.sayHello();
            }
        });
    }

    public interface View extends AbstractPresenter.View {
        void sayHello();
    }
}
