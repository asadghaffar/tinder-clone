package com.fcodex.winkiswipe.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.fcodex.winkiswipe.Activity.EditProfileActivity;
import com.fcodex.winkiswipe.Objects.UserObject;
import com.fcodex.winkiswipe.R;
import com.fcodex.winkiswipe.Activity.SettingsActivity;

/**
 * Activity responsible for displaying the current user and the buttons to go
 * to the settingsActivity and EditProfileActivity
 */
public class UserFragment extends Fragment {

    private View view;
    private TextView mName;
    private ImageView mProfileImage, mSettings, mEditProfile;
    public UnifiedNativeAd nativeAd;

    public UserFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_user, container, false);

        mName = view.findViewById(R.id.name);
        mProfileImage = view.findViewById(R.id.profileImage);

        //Call_BigGoogelNative();

        nativeAdMethod();

        mSettings = view.findViewById(R.id.settings);
        mEditProfile = view.findViewById(R.id.editProfile);

        mEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditProfileActivity.class);
            startActivity(intent);
        });
        mSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        getUserInfo();

        return view;
    }

    // Native Ad Start
    private void nativeAdMethod() {
        AdLoader.Builder builder = new AdLoader.Builder(getActivity(), getString(R.string.google_native));
        builder.forUnifiedNativeAd(unifiedNativeAd -> {
            if (nativeAd != null)
                nativeAd = unifiedNativeAd;
            CardView cardView = view.findViewById(R.id.main_ad_container);
            @SuppressLint("InflateParams") UnifiedNativeAdView adView = (UnifiedNativeAdView)
                    getLayoutInflater().inflate(R.layout.native_ad_layout, null);
            populateNativeAdView(unifiedNativeAd, adView);
            cardView.removeAllViews();
            cardView.addView(adView);
        });

        AdLoader adLoader = builder.withAdListener(new AdListener(){
            @Override
            public void onAdFailedToLoad(int i) {
                Toast.makeText(getActivity(), "Fail to load ad", Toast.LENGTH_SHORT).show();
                super.onAdFailedToLoad(i);
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());

    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd1, UnifiedNativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        adView.setBodyView(adView.findViewById(R.id.ad_text_body));
        adView.setStarRatingView(adView.findViewById(R.id.ad_star_rating));
        adView.setMediaView(adView.findViewById(R.id.ad_media_view));
        adView.setCallToActionView(adView.findViewById(R.id.ad_button_call_to_action_view));
        adView.setIconView(adView.findViewById(R.id.adv_icon));

        adView.getMediaView().setMediaContent(nativeAd1.getMediaContent());
        ((TextView) adView.getHeadlineView()).setText(nativeAd1.getHeadline());

        if (nativeAd1.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getBodyView()).setText(nativeAd1.getBody());
            adView.getBodyView().setVisibility(View.VISIBLE);
        }

        if (nativeAd1.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd1.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        if (nativeAd1.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd1.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd1.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView())
                    .setImageDrawable((nativeAd1.getIcon()).getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd1.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            ((Button) adView.getCallToActionView()).setText(nativeAd1.getCallToAction());
        }

        adView.setNativeAd(nativeAd1);

    }
    // Native Ad End

    /**
     * Fetches current user's info from the database
     */
    private void getUserInfo() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserObject mUser = new UserObject();
                mUser.parseObject(dataSnapshot);


                mName.setText(mUser.getName() + ", " + mUser.getAge());
                if (getContext() != null && !mUser.getProfileImageUrl().equals("default"))
                    Glide.with(getContext()).load(mUser.getProfileImageUrl()).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*private void Call_BigGoogelNative() {
        AdLoader adLoader = new AdLoader.Builder(getActivity(), getString(R.string.google_native))
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {

                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        Log.d("FBADS", "Googele ad is loaded and ready to be displayed!");
                        if (nativeAd != null) {
                            nativeAd.destroy();
                        }
                        nativeAd = unifiedNativeAd;
                        FrameLayout frameLayout = (FrameLayout) getActivity().findViewById(R.id.ad_view);
                        UnifiedNativeAdView unifiedNativeAdView = (UnifiedNativeAdView) getActivity().getLayoutInflater().inflate(R.layout.googleads, null);
                        populateUnifiedNativeAdView(unifiedNativeAd, unifiedNativeAdView);
                        frameLayout.removeAllViews();
                        frameLayout.addView(unifiedNativeAdView);
                    }
                }).withAdListener(
                        new AdListener() {
                            @Override
                            public void onAdFailedToLoad(int errorCode) {

                            }
                        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    public void populateUnifiedNativeAdView(UnifiedNativeAd unifiedNativeAd, UnifiedNativeAdView unifiedNativeAdView) {
        unifiedNativeAdView.setMediaView((com.google.android.gms.ads.formats.MediaView) unifiedNativeAdView.findViewById(R.id.ad_media));
        unifiedNativeAdView.setHeadlineView(unifiedNativeAdView.findViewById(R.id.ad_headline));
        unifiedNativeAdView.setBodyView(unifiedNativeAdView.findViewById(R.id.ad_body));
        unifiedNativeAdView.setIconView(unifiedNativeAdView.findViewById(R.id.ad_app_icon));
        unifiedNativeAdView.setPriceView(unifiedNativeAdView.findViewById(R.id.ad_price));
        unifiedNativeAdView.setStarRatingView(unifiedNativeAdView.findViewById(R.id.ad_stars));
        unifiedNativeAdView.setStoreView(unifiedNativeAdView.findViewById(R.id.ad_store));
        unifiedNativeAdView.setCallToActionView(unifiedNativeAdView.findViewById(R.id.ad_call_to_action));
        unifiedNativeAdView.setAdvertiserView(unifiedNativeAdView.findViewById(R.id.ad_advertiser));
        ((TextView) unifiedNativeAdView.getHeadlineView()).setText(unifiedNativeAd.getHeadline());
        if (unifiedNativeAd.getBody() == null) {
            unifiedNativeAdView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            unifiedNativeAdView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) unifiedNativeAdView.getBodyView()).setText(unifiedNativeAd.getBody());
        }
        if (unifiedNativeAd.getCallToAction() == null) {
            unifiedNativeAdView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            unifiedNativeAdView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) unifiedNativeAdView.getCallToActionView()).setText(unifiedNativeAd.getCallToAction());
        }
        if (unifiedNativeAd.getIcon() == null) {
            unifiedNativeAdView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) unifiedNativeAdView.getIconView()).setImageDrawable(unifiedNativeAd.getIcon().getDrawable());
            unifiedNativeAdView.getIconView().setVisibility(View.VISIBLE);
        }
        if (unifiedNativeAd.getPrice() == null) {
            unifiedNativeAdView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            unifiedNativeAdView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) unifiedNativeAdView.getPriceView()).setText(unifiedNativeAd.getPrice());
        }
        if (unifiedNativeAd.getStore() == null) {
            unifiedNativeAdView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            unifiedNativeAdView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) unifiedNativeAdView.getStoreView()).setText(unifiedNativeAd.getStore());
        }
        if (unifiedNativeAd.getStarRating() == null) {
            unifiedNativeAdView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) unifiedNativeAdView.getStarRatingView()).setRating(unifiedNativeAd.getStarRating().floatValue());
            unifiedNativeAdView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        if (unifiedNativeAd.getAdvertiser() == null) {
            unifiedNativeAdView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) unifiedNativeAdView.getAdvertiserView()).setText(unifiedNativeAd.getAdvertiser());
            unifiedNativeAdView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        unifiedNativeAdView.setNativeAd(unifiedNativeAd);
        VideoController videoController = unifiedNativeAd.getVideoController();
        if (videoController.hasVideoContent()) {
            videoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                public void onVideoEnd() {
                    super.onVideoEnd();
                }
            });
        }
    }*/

}