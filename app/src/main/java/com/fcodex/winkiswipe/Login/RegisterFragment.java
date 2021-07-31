package com.fcodex.winkiswipe.Login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.fcodex.winkiswipe.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment Responsible for registering a new user
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {

    EditText mName,
            mEmail,
            mPassword;
    Button mRegister;

    SegmentedButtonGroup mRadioGroup;

    private InterstitialAd interstitialAd;

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.fragment_registration, container, false);
        else
            container.removeView(view);



        facebookAD();

        return view;
    }

    private void facebookAD() {
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(getActivity());
        interstitialAd = new InterstitialAd(getActivity(), "303762787757470_345151193618629");
        // Create listeners for the Interstitial Ad
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.d("adDisplay_", "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.d("adDismiss", "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.d("adFailed", "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d("alreadyDisplayed", "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d("adClicked", "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d("impressionLog", "Interstitial ad impression logged!");
            }
        };

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        interstitialAd.loadAd(interstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener).build());
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeObjects();
    }


    /**
     * Register the user, but before that check if every field is correct.
     * After that registers the user and creates an entry for it oin the database
     */
    private void register() {
        if (mEmail.getText().length() == 0) {
            mEmail.setError("please fill this field");
            return;
        }
        if (mName.getText().length() == 0) {
            mName.setError("please fill this field");
            return;
        }
        if (mPassword.getText().length() == 0) {
            mPassword.setError("please fill this field");
            return;
        }
        if (mPassword.getText().length() < 6) {
            mPassword.setError("password must have at least 6 characters");
            return;
        }


        final String name = mName.getText().toString();
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        final String accountType;
        int selectId = mRadioGroup.getPosition();

        switch (selectId) {
            case 1:
                accountType = "Female";
                break;
            default:
                accountType = "Male";
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Snackbar.make(view.findViewById(R.id.layout), "sign up error", Snackbar.LENGTH_SHORT).show();
                } else {
                    String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Map userInfo = new HashMap();
                    userInfo.put("name", name);
                    userInfo.put("sex", accountType);
                    userInfo.put("search_distance", 100);
                    userInfo.put("profileImageUrl", "default");
                    switch (accountType) {
                        case "Male":
                            userInfo.put("interest", "Female");
                            break;
                        case "Female":
                            userInfo.put("interest", "Male");
                            break;
                    }
                    FirebaseDatabase.getInstance().getReference().child("Users").child(user_id).updateChildren(userInfo);
                }
            }
        });

    }

    /**
     * Initializes the design Elements and calls clickListeners for them
     */
    private void initializeObjects() {
        mEmail = view.findViewById(R.id.email);
        mName = view.findViewById(R.id.name);
        mPassword = view.findViewById(R.id.password);
        mRegister = view.findViewById(R.id.register);
        mRadioGroup = view.findViewById(R.id.radioRealButtonGroup);

        mRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.register:
                register();
                ;
        }
    }
}