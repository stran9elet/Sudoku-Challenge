package com.example.sudokuchallenge.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokuchallenge.R;

public class BlankActivity extends AppCompatActivity {
    Handler handler;
    Runnable runnable;
    private static final int BACK_TO_MAIN_ACTIVITY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        Intent intent = getIntent();
        String target = intent.getStringExtra("target");

        switch (target) {
            case "GameActivity":
                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent1 = new Intent(BlankActivity.this, GameActivity.class);
                        startActivityForResult(intent1, 1000);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                };
                handler.postDelayed(runnable, 850); //This much time your blank activity will remain open
                break;

//            case "MainActivity":
//                handler = new Handler();
//                runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent1 = new Intent(BlankActivity.this, MainActivity.class);
//                        startActivity(intent1);
//                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                        finish();
//                    }
//                };
//                handler.postDelayed(runnable, 850); //This much time your blank activity will remain open
//                break;
            default:
                handler.removeCallbacks(runnable);
                finish();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                break;

        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BACK_TO_MAIN_ACTIVITY){

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            };
            handler.postDelayed(runnable, 150); //This much time your blank activity will remain open

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(runnable);

        /*
        So that if the user presses back from the BlankActivity,
        then still the next activity doesn't pop up since intent execution was going in another thread
         */
    };
}

