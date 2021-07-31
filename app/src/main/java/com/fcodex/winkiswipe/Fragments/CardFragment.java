package com.fcodex.winkiswipe.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.fcodex.winkiswipe.Objects.UserObject;
import com.fcodex.winkiswipe.Adapter.CardAdapter;
import com.fcodex.winkiswipe.Activity.MainActivity;
import com.fcodex.winkiswipe.R;
import com.fcodex.winkiswipe.Utils.SendNotification;
import com.fcodex.winkiswipe.Activity.ZoomCardActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the cards to the user
 *
 * It displays them in a way that is within the search params of the current logged in user
 */
public class CardFragment  extends Fragment {

    private CardAdapter cardAdapter;
    int searchDistance = 100;
    int swipe_count = 0;

    private FirebaseAuth mAuth;

    private String currentUId;

    private DatabaseReference usersDb;
    private RewardedAd rewardedAd;
    private RewardedVideoAd mRewardedVideoAd;
    public List<Object> mRecyclerViewItems;

    List<UserObject> rowItems;

    View view;

    public CardFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_card, container, false);

        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
//        loadRewardedVideoAd();

        rewardedAd = new RewardedAd(getActivity(), getString(R.string.google_reward));
//        call_RewardAds();


        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null)
            return view;
        currentUId = mAuth.getCurrentUser().getUid();

        fetchUserSearchParams();

        rowItems = new ArrayList<>();

        cardAdapter = new CardAdapter(getContext(), R.layout.item_card, rowItems );

        final SwipeFlingAdapterView flingContainer = view.findViewById(R.id.frame);

        flingContainer.setAdapter(cardAdapter);

        //Handling swipe of cards
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                Log.d("LIST", "removed object!");
                rowItems.remove(0);
                cardAdapter.notifyDataSetChanged();

                swipe_count++;
                Log.d("SWIPE", String.valueOf(swipe_count));
                if (swipe_count>=2){
//                    call_RewardAds();

                    call_showDialogue();

                }

            }

            @Override
            public void onLeftCardExit(Object dataObject) {

                UserObject obj = (UserObject) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);

            }

            @Override
            public void onRightCardExit(Object dataObject) {
                UserObject obj = (UserObject) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("yeps").child(currentUId).setValue(true);
                isConnectionMatch(userId);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener((itemPosition, dataObject) -> {
            UserObject UserObject = (UserObject) dataObject;
            Intent i = new Intent(getContext(), ZoomCardActivity.class);
            i.putExtra("UserObject", UserObject);
            startActivity(i);
        });




        FloatingActionButton fabLike = view.findViewById(R.id.fabLike);
        FloatingActionButton fabNope = view.findViewById(R.id.fabNope);

        //Listeners for the fab buttons, they do the same as the swipe feature, but withe the click of the buttons
        fabLike.setOnClickListener(v -> {
            if(rowItems.size()!=0)
                flingContainer.getTopCardListener().selectRight();
        });
        fabNope.setOnClickListener(v -> {
            if(rowItems.size()!=0)
                flingContainer.getTopCardListener().selectLeft();
        });

        return view;
    }

    private void call_showDialogue() {

        final Dialog successDialog = new Dialog(getActivity());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.dialogue_demo);
        successDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        successDialog.setCancelable(true);

        Button btn_showads =  successDialog.findViewById(R.id.btn_showads);

        btn_showads.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                successDialog.dismiss();

                loadRewardedVideoAd();
                swipe_count=0;

            }
        });

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        successDialog.getWindow().setLayout(width, height);
        successDialog.show();

    }


    GeoQuery geoQuery;
    /**
     * Fetch closest users to the current user using a GeoQuery.
     *
     * The users found are within a radius defined in the SearchObject and the center of
     * the radius is the current user's location
     * @param lastKnowLocation - user last know location
     */
    public void getCloseUsers(Location lastKnowLocation){
        rowItems.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("location");
        GeoFire geoFire = new GeoFire(ref);

        if(geoQuery!=null)
            geoQuery.removeAllListeners();
        geoQuery = geoFire.queryAtLocation(new GeoLocation(lastKnowLocation.getLatitude(),lastKnowLocation.getLongitude()), 100);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                getUsersInfo(key);
            }
            @Override
            public void onKeyExited(String key) {
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }
            @Override
            public void onGeoQueryReady() {
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.google_reward),
                new AdRequest.Builder().build());

        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                if (mRewardedVideoAd.isLoaded()){
                    mRewardedVideoAd.show();
                }
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.d("GoogelAds","Reward Ads Open");
            }

            @Override
            public void onRewardedVideoStarted() {
                Log.d("GoogelAds","Reward Ads Started");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                Log.d("GoogelAds","Reward Ads Close");
            }

            @Override
            public void onRewarded(com.google.android.gms.ads.reward.RewardItem rewardItem) {
                Log.d("GoogelAds","Reward Ads Rewarded. ");
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Log.d("GoogelAds","Reward Ads Fail to load. "+i);
            }

            @Override
            public void onRewardedVideoCompleted() {
                Log.d("GoogelAds","Reward Ads Completed");
            }
        });
    }

    /**
     * Checks if new connection is a match if it is then add it to the database and create a new chat
     * @param userId
     */
    private void isConnectionMatch(String userId) {
        DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yeps").child(userId);
        currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Toast.makeText(getContext(), "new Connection", Toast.LENGTH_LONG).show();

                    String key = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey();

                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child("ChatId").setValue(key);
                    usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(key);

                    SendNotification sendNotification = new SendNotification();
                    sendNotification.SendNotification("check it out!", "new Connection!", dataSnapshot.getKey());

                    Snackbar.make(view.findViewById(R.id.layout), "new Connection!", Snackbar.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private String  userInterest = "Male";

    /**
     * Fetches user search params from the database
     *
     * After that call isLocationEnabled which will see if the location services are enabled
     * and then fetch the last location known.
     */
    private void fetchUserSearchParams(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.child("interest").getValue() != null)
                        userInterest = dataSnapshot.child("interest").getValue().toString();
                    if (dataSnapshot.child("search_distance").getValue() != null)
                        searchDistance = Integer.parseInt(dataSnapshot.child("search_distance").getValue().toString());

                    ((MainActivity)getActivity()).isLocationEnable();
                    rowItems.clear();
                    cardAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Get info of a user and check if that user is within the search params, if it is then
     * add it to the list and update the adapter.
     *
     * Does not add the user if it is already a connection.
     * @param userId - id of the user that's a possible user to display the card of
     */
    private void getUsersInfo(String userId){
        for(UserObject mCard : rowItems){
            if(mCard.getUserId().equals(userId)){return;}
        }
        usersDb.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){return;}

                for(UserObject mCard : rowItems){
                    if(mCard.getUserId().equals(dataSnapshot.getKey())){return;}
                }

                UserObject mUser = new UserObject();
                mUser.parseObject(dataSnapshot);

                if(mUser.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){return;}
                if(dataSnapshot.child("connections").child("nope").hasChild(currentUId)){return;}
                if(dataSnapshot.child("connections").child("yeps").hasChild(currentUId)) {return;}
                if(!mUser.getUserSex().equals(userInterest) && !userInterest.equals("Both")){return;}

                for(UserObject mCard : rowItems){
                    if(mCard.getUserId().equals(userId)){return;}
                }

                rowItems.add(mUser);
                cardAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}