package com.github.florent37.androidmvpresenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import florent37.github.com.rxlifecycle.RxLifecycle;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by florentchampigny on 16/08/2017.
 */

public class Player {
    public static Observable<Boolean> playObservable(final LifecycleOwner lifecycleOwner, final Context context, final String mp3) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Boolean> e) throws Exception {
                try {
                    final MediaPlayer mediaPlayer = new MediaPlayer();

                    AssetFileDescriptor descriptor = context.getAssets().openFd(mp3);
                    mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    descriptor.close();

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.release();
                            e.onNext(true);
                        }
                    });

                    mediaPlayer.prepare();
                    mediaPlayer.setVolume(1f, 1f);
                    mediaPlayer.start();

                    RxLifecycle.with(lifecycleOwner)
                            .onPause()
                            .subscribe(new Consumer<Lifecycle.Event>() {
                                @Override
                                public void accept(Lifecycle.Event event) throws Exception {
                                    mediaPlayer.stop();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    throwable.printStackTrace();
                                }
                            });

                    RxLifecycle.with(lifecycleOwner)
                            .onResume()
                            .subscribe(new Consumer<Lifecycle.Event>() {
                                @Override
                                public void accept(Lifecycle.Event event) throws Exception {
                                    mediaPlayer.start();
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    throwable.printStackTrace();
                                }
                            });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    e.onNext(true);
                }
            }
        });
    }

    public static void play(final LifecycleOwner lifecycleOwner, final Context context, final String mp3) {
        playObservable(lifecycleOwner, context, mp3).subscribe();
    }

    public static void playInfinite(final LifecycleOwner lifecycleOwner, final Context context, final String mp3) {
        playObservable(lifecycleOwner, context, mp3)
                .subscribe(
                        new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean $) throws Exception {
                                play(lifecycleOwner, context, mp3);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
    }
}
