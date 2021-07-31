package com.fcodex.winkiswipe.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.fcodex.winkiswipe.Login.LauncherActivity;
import com.fcodex.winkiswipe.R;

public class LoadingSplashScreen extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;
    private int status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_splash_screen);

        interstitialAdMethod();
        splashScreen();

    }

    private void splashScreen() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(LoadingSplashScreen.this, LauncherActivity.class);
            intent.putExtra("EXIT", true);
            startActivity(intent);

        }, 5000);
    }

    // Interstitial ad start
    private void interstitialAdMethod() {
        mInterstitialAd = new com.google.android.gms.ads.InterstitialAd(this);
        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.google_intersitial));

        AdRequest adRequest = new AdRequest.Builder().build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
    // Interstitial ad end
}