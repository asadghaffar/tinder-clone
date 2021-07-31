package com.fcodex.winkiswipe.Objects;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;

/**
 * Object of each card
 */
public class UserObject implements Serializable {
    private String  userId = "--",
                    name = "--",
                    profileImageUrl = "default",
                    age = "--",
                    about = "--",
                    job = "--",
                    userSex = "--",
                    interest = "--",
                    phone = "--";

    float searchDistance = 100;

    public UserObject(){}


    public void parseObject(DataSnapshot dataSnapshot){
        if(!dataSnapshot.exists()){return;}
        userId = dataSnapshot.getKey();

        if(dataSnapshot.child("name").getValue()!=null)
            name = dataSnapshot.child("name").getValue().toString();
        if(dataSnapshot.child("sex").getValue()!=null)
            userSex = dataSnapshot.child("sex").getValue().toString();
        if(dataSnapshot.child("age").getValue()!=null)
            age = dataSnapshot.child("age").getValue().toString();
        if(dataSnapshot.child("job").getValue()!=null)
            job = dataSnapshot.child("job").getValue().toString();
        if(dataSnapshot.child("about").getValue()!=null)
            about = dataSnapshot.child("about").getValue().toString();
        if (dataSnapshot.child("profileImageUrl").getValue()!=null)
            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
        if (dataSnapshot.child("phone").getValue()!=null)
            phone = dataSnapshot.child("phone").getValue().toString();

        if(dataSnapshot.child("interest").getValue()!=null)
            interest = dataSnapshot.child("interest").getValue().toString();
        if(dataSnapshot.child("search_distance").getValue()!=null)
            searchDistance = Float.parseFloat(dataSnapshot.child("search_distance").getValue().toString());

    }

    public String getUserId(){
        return userId;
    }
    public String getName(){
        return name;
    }
    public String getAge(){
        return age;
    }
    public String getAbout(){
        return about;
    }
    public String getJob(){
        return job;
    }
    public String getProfileImageUrl(){
        return profileImageUrl;
    }

    public float getSearchDistance() {
        return searchDistance;
    }

    public String getInterest() {
        return interest;
    }

    public String getUserSex() {
        return userSex;
    }

    public String getPhone() {
        return phone;
    }
}
