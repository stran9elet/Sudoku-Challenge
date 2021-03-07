package com.example.sudokuchallenge.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokuchallenge.R;
import com.example.sudokuchallenge.models.User;
import com.google.gson.Gson;

public class SigningActivity extends AppCompatActivity {

    public static final String USER_DATA_PREFERENCE_KEY = "user data";
    public static final String USER_KEY = "user";
    private String nickname = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);


        EditText nicknameEditText = findViewById(R.id.nickname_text);

        SharedPreferences sharedPreferences = getSharedPreferences(USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
        if(sharedPreferences.contains(USER_KEY)){
            Gson gson = new Gson();
            User user = gson.fromJson(sharedPreferences.getString(USER_KEY, null), User.class);
            User.setCurrentUser(user);
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
//                    dataEditor.putString(NICKNAME_KEY, nickname);
//                    dataEditor.apply();
                    User user = new User(nickname, 0);
                    User.setCurrentUser(user);
                    Gson gson = new Gson();
                    String jsonUser = gson.toJson(user);
                    dataEditor.putString(USER_KEY, jsonUser);
                    dataEditor.apply();
                    Intent intent = new Intent(SigningActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(SigningActivity.this, "Enter a valid nickname first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}