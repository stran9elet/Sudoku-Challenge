package com.example.sudokuchallenge.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.example.sudokuchallenge.activities.SigningActivity;
import com.example.sudokuchallenge.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;


public class UserPoster {

    private static String previousUserString = "";
    private static Handler handler;
    private static Runnable runnable;
    public static boolean enabled = false;

    public static void init(Context context){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    if(enabled) {
                        if (auth.getCurrentUser() != null) {
                            SharedPreferences userPreferences = context.getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, Context.MODE_PRIVATE);
                            DatabaseReference userReference = database.getReference().child("Users").child(auth.getCurrentUser().getUid());
                            String stringUser = userPreferences.getString(SigningActivity.USER_KEY, null);
                            if (!previousUserString.equals(stringUser)) {
                                if (isNetworkAvailable(context)) {
                                    previousUserString = stringUser;
                                    User user = new Gson().fromJson(stringUser, User.class);
                                    userReference.setValue(user);
                                }
                            }
                        }
                    }
                    handler.postDelayed(this, 1000);
                }
            };
            handler.post(runnable);
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
