package com.fcodex.winkiswipe.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.fcodex.winkiswipe.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splashScreen();

    }

    private void splashScreen() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this, LoadingSplashScreen.class);
            startActivity(intent);

        }, 2000);
    }
}