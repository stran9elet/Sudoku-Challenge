package com.example.sudokuchallenge.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokuchallenge.R;
import com.thekhaeng.pushdownanim.PushDownAnim;

public class MainActivity extends AppCompatActivity {

    private ImageView newGameButton;
    private ImageView resumeGameButton;
    private ImageView tournamentButton;
    private ImageView extraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showBoardDrop(); // to show animation

        newGameButton = (ImageView) findViewById(R.id.new_game_button);
        resumeGameButton = (ImageView) findViewById(R.id.resume_game_button);
        tournamentButton = (ImageView) findViewById(R.id.tournament_button);
        extraButton = (ImageView) findViewById(R.id.extra_button);

        PushDownAnim.setPushDownAnimTo(newGameButton, resumeGameButton, tournamentButton, extraButton)
                .setScale(0.8f);

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlankActivity.class);
                intent.putExtra("target", "GameActivity");
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
//                finish();
            }
        });

//        resumeGameButton.setVisibility(View.INVISIBLE);
//        extraButton.setVisibility(View.INVISIBLE);

    }



    private void showBoardDrop(){
        ImageView sudokuNameBoard = (ImageView) findViewById(R.id.sudokuNameBoard);
        sudokuNameBoard.clearAnimation();
        TranslateAnimation transAnim = new TranslateAnimation(0, 0, -1*getDisplayHeight(),0);
        transAnim.setStartOffset(250);
        transAnim.setDuration(1800);
        transAnim.setFillAfter(true);
        transAnim.setInterpolator(new BounceInterpolator());
        transAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                Log.i("TAG", "Starting button dropdown animation");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.i("TAG",
                        "Ending button dropdown animation. Clearing animation and setting layout");
                sudokuNameBoard.clearAnimation();
                final int left = sudokuNameBoard.getLeft();
                final int top = sudokuNameBoard.getTop();
                final int right = sudokuNameBoard.getRight();
                final int bottom = sudokuNameBoard.getBottom();
                sudokuNameBoard.layout(left, top, right, bottom);

            }
        });

        sudokuNameBoard.startAnimation(transAnim);
    }

    private int getDisplayHeight() {
        return this.getResources().getDisplayMetrics().heightPixels;
    }

}
