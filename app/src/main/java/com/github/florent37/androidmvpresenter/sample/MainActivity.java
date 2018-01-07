package com.github.florent37.androidmvpresenter.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements MainPresenter.View {

    MainPresenter presenter;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text);

        presenter = new MainPresenter(new AuthRepoImpl()); //inject if you can
        presenter.bind(this, this);
    }

    @Override
    public void sayHello() {
        textView.setText("hello");
    }
}
