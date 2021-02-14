package com.example.sudokuchallenge.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokuchallenge.Classes.SudokuMaker;
import com.example.sudokuchallenge.R;
import com.thekhaeng.pushdownanim.PushDownAnim;

public class MainActivity extends AppCompatActivity {

    private ImageView newGameButton;
    private ImageView resumeGameButton;
    private ImageView tournamentButton;
    private ImageView extraButton;

    private TextView easyTextView;
    private TextView mediumTextView;
    private TextView difficultTextView;
    private TextView timeAttackTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showBoardDrop(); // to show animation

        newGameButton = (ImageView) findViewById(R.id.new_game_button);
        resumeGameButton = (ImageView) findViewById(R.id.resume_game_button);
        tournamentButton = (ImageView) findViewById(R.id.tournament_button);
        extraButton = (ImageView) findViewById(R.id.extra_button);

        SharedPreferences sharedPreferences = getSharedPreferences(GameActivity.PREFERENCE_KEY, MODE_PRIVATE);
        String jsonMakerResponse = sharedPreferences.getString(GameActivity.JSON_MAKER_RESPONSE_KEY, "");
        boolean setConfirmationDialog = false;
        if(jsonMakerResponse.equals("")) {
            resumeGameButton.setVisibility(View.INVISIBLE);
        } else {
            resumeGameButton.setVisibility(View.VISIBLE);
            setConfirmationDialog = true;
        }

        PushDownAnim.setPushDownAnimTo(newGameButton, resumeGameButton, tournamentButton, extraButton)
                .setScale(0.8f);

        boolean finalSetConfirmationDialog = setConfirmationDialog;
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalSetConfirmationDialog)
                    setConfirmation();
                else
                    selectDifficulty();
            }
        });


        resumeGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlankActivity.class);
                intent.putExtra("target", "GameActivity");
                intent.putExtra("gameType", "resume game");
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();
            }
        });
    }

    private void setConfirmation() {
        Dialog confirmationDialog = new Dialog(MainActivity.this);
        confirmationDialog.setContentView(R.layout.confirm_new_game);
        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        confirmationDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // to set the width and height of the view
        //ToDo: Show pop animation when showing this dialog
        confirmationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        confirmationDialog.setCancelable(true);
        confirmationDialog.show();

        TextView confirmationYesView = (TextView) confirmationDialog.findViewById(R.id.confirmation_yes);
        TextView confirmationNoView = (TextView) confirmationDialog.findViewById(R.id.confirmation_no);

        confirmationYesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.dismiss();
                selectDifficulty();
            }
        });

        confirmationNoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.dismiss();
            }
        });
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

    private void selectDifficulty(){
        Dialog difficultyLevelDialog = new Dialog(MainActivity.this);
        difficultyLevelDialog.setContentView(R.layout.difficulty_layout);
        difficultyLevelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        difficultyLevelDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // to set the width and height of the view
        difficultyLevelDialog.getWindow().setGravity(Gravity.BOTTOM); // to attach the dialog to bottom
        //ToDo: Show slide up animation when showing this dialog
        difficultyLevelDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        difficultyLevelDialog.setCancelable(true);
        difficultyLevelDialog.show();

        easyTextView = (TextView) difficultyLevelDialog.findViewById(R.id.easy_difficulty_view);
        easyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlankActivity.class);
                intent.putExtra("target", "GameActivity");
                intent.putExtra("gameType", "new game");
                intent.putExtra("difficulty", SudokuMaker.EASY);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        mediumTextView = (TextView) difficultyLevelDialog.findViewById(R.id.medium_difficulty_view);
        mediumTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlankActivity.class);
                intent.putExtra("target", "GameActivity");
                intent.putExtra("gameType", "new game");
                intent.putExtra("difficulty", SudokuMaker.MEDIUM);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });


        difficultTextView = (TextView) difficultyLevelDialog.findViewById(R.id.difficult_difficulty_view);
        difficultTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlankActivity.class);
                intent.putExtra("target", "GameActivity");
                intent.putExtra("gameType", "new game");
                intent.putExtra("difficulty", SudokuMaker.DIFFICULT);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });


        timeAttackTextView = (TextView) difficultyLevelDialog.findViewById(R.id.time_attack_difficulty_view);
        timeAttackTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BlankActivity.class);
                intent.putExtra("target", "GameActivity");
                intent.putExtra("gameType", "new game");
                intent.putExtra("difficulty", "time attack");
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
    }

    private int getDisplayHeight() {
        return this.getResources().getDisplayMetrics().heightPixels;
    }

}
