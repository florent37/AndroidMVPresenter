package com.github.florent37.androidmvpresenter;

public class ClickListenerWrapper<T> {
    T listener;

    public void setListener(T listener){
        this.listener = listener;
    }

    public interface Callback<T> {
        void onClick(T t);
    }

    public void onClick(Callback<T> callback){
        if (listener != null) {
            callback.onClick(listener);
        }
    }
}
