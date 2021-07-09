package com.strangelet.sudokuchallenge.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.strangelet.sudokuchallenge.R;
import com.strangelet.sudokuchallenge.models.User;
import com.google.gson.Gson;

public class SigningActivity extends AppCompatActivity {

    public static final String USER_DATA_PREFERENCE_KEY = "user data";
    public static final String USER_KEY = "user";
    public static final String MUSIC_SETTINGS_KEY = "music settings";
    public static final String SFX_SETTINGS_KEY = "sfx settings";
    private String nickname = "";
    private static final int MAX_VOLUME = 100;

    private MediaPlayer northPlayer;
    private MediaPlayer naturePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);


        EditText nicknameEditText = findViewById(R.id.nickname_text);

        SharedPreferences sharedPreferences = getSharedPreferences(USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
        if(sharedPreferences.contains(USER_KEY)){
            Intent intent = new Intent(SigningActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        RelativeLayout signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nickname = nicknameEditText.getText().toString().trim();
                if(nickname.length() > 0) {
                    SharedPreferences.Editor dataEditor= sharedPreferences.edit();
                    User user = new User(nickname, 0);
                    user.boughtWoodenBoard = true;
                    Gson gson = new Gson();
                    String jsonUser = gson.toJson(user);
                    dataEditor.putString(USER_KEY, jsonUser);
                    dataEditor.apply();
                    Intent intent = new Intent(SigningActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                    SharedPreferences.Editor settingsEditor = getSharedPreferences(USER_DATA_PREFERENCE_KEY, MODE_PRIVATE).edit();
                    settingsEditor.putBoolean(MUSIC_SETTINGS_KEY, true);
                    settingsEditor.putBoolean(SFX_SETTINGS_KEY, true);
                    settingsEditor.apply();
                }else {
                    Toast.makeText(SigningActivity.this, "Enter a valid nickname first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        playBackgroundMusic();
        playNatureSounds();
    }

    private void setCurrentVolume(int currVolume){
        if(northPlayer!=null) {
            final float volume = (float) (1 - (Math.log(MAX_VOLUME - currVolume) / Math.log(MAX_VOLUME)));
            northPlayer.setVolume(volume, volume); //set volume takes two paramater
        }
    }

    private void fadeOutAudio(){
        final int[] i = {99};
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(i[0]>0) {
                    setCurrentVolume(i[0]);
                    i[0] = i[0] - 1;
                    handler.postDelayed(this, 7);
                } else {
                    if(northPlayer!=null) {
                        northPlayer.stop();
                        northPlayer.release();
                        northPlayer = null;
                    }
                }
            }
        };
        handler.post(runnable);
    }

    private void playBackgroundMusic(){
        if(northPlayer == null)
            northPlayer = MediaPlayer.create(this, R.raw.northern_lights);

        northPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playBackgroundMusic();
            }
        });

        northPlayer.start();
    }

    private void playNatureSounds(){
        if(naturePlayer == null)
            naturePlayer = MediaPlayer.create(this, R.raw.nature_sound);
        naturePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNatureSounds();
            }
        });

        naturePlayer.start();
    }

    private void fadeStopMediaPlayer(){
        if(northPlayer != null){
            fadeOutAudio();
        }

        if(naturePlayer != null){
            naturePlayer.stop();
            naturePlayer.release();
            naturePlayer = null;
        }
    }

    private void stopMediaPlayer(){
        if(northPlayer != null){
            northPlayer.stop();
            northPlayer.release();
            northPlayer = null;
        }

        if(naturePlayer != null){
            naturePlayer.stop();
            naturePlayer.release();
            naturePlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMediaPlayer();
    }

    @Override
    public void finish() {
        super.finish();
        fadeStopMediaPlayer();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}