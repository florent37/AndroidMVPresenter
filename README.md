# AndroidMVPresenter

<a href="https://goo.gl/WXW8Dc">
  <img alt="Android app on Google Play" src="https://developer.android.com/images/brand/en_app_rgb_wo_45.png" />
</a>

# Download

<a href='https://ko-fi.com/A160LCC' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://az743702.vo.msecnd.net/cdn/kofi1.png?v=0' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

[ ![Download](https://api.bintray.com/packages/florent37/maven/androidmvpresenter/images/download.svg) ](https://bintray.com/florent37/maven/androidmvpresenter/_latestVersion)
```java
dependencies {
    compile 'com.github.florent37:androidmvpresenter:1.0.1'
}
```


# Presenter

```java
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
```