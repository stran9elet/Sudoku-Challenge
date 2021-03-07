package com.example.sudokuchallenge.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokuchallenge.R;
import com.example.sudokuchallenge.customViews.SudokuBoard;
import com.example.sudokuchallenge.utils.SudokuMaker;
import com.google.gson.Gson;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.Locale;

public class GameActivity extends AppCompatActivity {
    //ToDo: make bigger buttons for pixel and other such devices

    public static final String PREFERENCE_KEY = "sudoku_objects";
    public static final String JSON_MAKER_RESPONSE_KEY = "jsonMakerResponse";
    SudokuMaker sudokuMaker;
    SudokuBoard sudokuBoard;
    private TextView timeTextView;
    private ImageView timeOutTextView;

    private String gameType;
    private int difficulty;
    private String time;
    private Handler timeHandler;
    private Runnable timeRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ImageView backImage = (ImageView) findViewById(R.id.back_button);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView settingsImage = findViewById(R.id.settings_button);
        settingsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: open settings dialog
            }
        });

        gameType = getIntent().getStringExtra("gameType");
        sudokuBoard = (SudokuBoard) findViewById(R.id.sudoku_board);

        if (gameType.equals("new game")) {
            difficulty = getIntent().getIntExtra("difficulty", SudokuMaker.EASY);
            sudokuBoard.setDifficulty(difficulty);
            sudokuMaker = sudokuBoard.getSudokuMaker();
            if (difficulty == SudokuMaker.TIME_ATTACK) {
                sudokuMaker.timeLimit = 30*60;
            }
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String jsonMakerResponse = gson.toJson(sudokuMaker, SudokuMaker.class);
            editor.putString(JSON_MAKER_RESPONSE_KEY, jsonMakerResponse);
            editor.apply();
        }

        if (gameType.equals("resume game")) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
            String jsonMakerResponse = sharedPreferences.getString(JSON_MAKER_RESPONSE_KEY, "");
            Gson gson = new Gson();
            sudokuMaker = gson.fromJson(jsonMakerResponse, SudokuMaker.class);
            sudokuBoard.isResumedActivity(true, sudokuMaker);
        }


        ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
        ImageView imageView3 = (ImageView) findViewById(R.id.imageView3);
        ImageView imageView4 = (ImageView) findViewById(R.id.imageView4);
        ImageView imageView5 = (ImageView) findViewById(R.id.imageView5);
        ImageView imageView6 = (ImageView) findViewById(R.id.imageView6);
        ImageView imageView7 = (ImageView) findViewById(R.id.imageView7);
        ImageView imageView8 = (ImageView) findViewById(R.id.imageView8);
        ImageView imageView9 = (ImageView) findViewById(R.id.imageView9);
        ImageView resetImage = (ImageView) findViewById(R.id.reset_image);
        ImageView undoImage = (ImageView) findViewById(R.id.undo_image);
        ImageView redoImage = (ImageView) findViewById(R.id.redo_image);

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(1);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(2);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(3);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(4);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(5);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(6);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(7);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(8);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(9);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
//                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
                    showFinishing();
            }
        });
        resetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(SudokuMaker.RESET);
                sudokuBoard.invalidate();
            }
        });
        undoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(SudokuMaker.UNDO);
                sudokuBoard.invalidate();
            }
        });
        redoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(SudokuMaker.REDO);
                sudokuBoard.invalidate();
            }
        });

        PushDownAnim.setPushDownAnimTo(imageView1, imageView2, imageView3, imageView4, imageView5,
                imageView6, imageView7, imageView8, imageView9, undoImage, resetImage, redoImage, backImage).setScale(0.8f);

        timeTextView = (TextView) findViewById(R.id.time_view);
        timeOutTextView = (ImageView) findViewById(R.id.timeout_view);
        runStopwatch();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonMakerResponse = gson.toJson(sudokuMaker, SudokuMaker.class);
        editor.putString(JSON_MAKER_RESPONSE_KEY, jsonMakerResponse);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, BlankActivity.class);
        intent.putExtra("target", "MainActivity");
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private boolean checkSudoku(int[][] workingArray, int[][] fullArray) {
        for (int i = 0; i < fullArray.length; i++) {
            for (int j = 0; j < fullArray[i].length; j++) {
                if (fullArray[i][j] != workingArray[i][j])
                    return false;
            }
        }
        return true;
    }

    //ToDo: Show loading when making sudoku
    private void loadSudoku() {
    }

    private void runStopwatch() {
        timeHandler = new Handler();
        timeRunner = new Runnable() {
            @Override
            public void run() {
                if (sudokuMaker.timeLimit == 0) {
                    int hours = sudokuMaker.secondsElapsed / 3600;
                    int minutes = (sudokuMaker.secondsElapsed % 3600) / 60;
                    int seconds = sudokuMaker.secondsElapsed % 60;
                    time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                    timeTextView.setText(time);
                    timeHandler.postDelayed(this, 1000);
                    sudokuMaker.secondsElapsed++;
                } else {
                    int hours = (sudokuMaker.timeLimit - sudokuMaker.secondsElapsed) / 3600;
                    int minutesLeft = ((sudokuMaker.timeLimit - sudokuMaker.secondsElapsed) % 3600) / 60;
                    int secondsLeft = (sudokuMaker.timeLimit - sudokuMaker.secondsElapsed) % 60;
                    String time = String.format(Locale.getDefault(), "%02d:%02d", minutesLeft, secondsLeft);
                    timeTextView.setText(time);
                    if (sudokuMaker.secondsElapsed == sudokuMaker.timeLimit) {
                        sudokuMaker.timeLimit = 0;
                        timeOutTextView.setVisibility(View.VISIBLE);
                        showTimeout();
                    }
                    timeHandler.postDelayed(this, 1000);
                    sudokuMaker.secondsElapsed++;
                }
            }
        };
        timeHandler.post(timeRunner);
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

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void showTimeout() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_anim);
        animation.setInterpolator(new BounceInterpolator());

        Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.slide_to_top);
        animation1.setInterpolator(new AccelerateDecelerateInterpolator());

        animation.reset();
        timeOutTextView.clearAnimation();
        timeOutTextView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation1.reset();
                timeOutTextView.clearAnimation();
                timeOutTextView.startAnimation(animation1);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void showFinishing(){
        timeHandler.removeCallbacks(timeRunner);
        sudokuMaker.finished(true);
        sudokuBoard.invalidate();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.end_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        dialog.show();

        ImageView viewGame = dialog.findViewById(R.id.end_back_btn);
        TextView playAgain = dialog.findViewById(R.id.end_play_again);
        TextView returnHome = dialog.findViewById(R.id.end_return_home);
        TextView timeTaken = dialog.findViewById(R.id.end_time_taken);
        timeTaken.setText("Time taken: " + time);

        PushDownAnim.setPushDownAnimTo(viewGame, playAgain, returnHome).setScale(0.8f);

        viewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                LinearLayout linearLayout = findViewById(R.id.btn_row_1);
                LinearLayout linearLayout1 = findViewById(R.id.btn_row_2);
                LinearLayout linearLayout2 = findViewById(R.id.btn_row_3);

                linearLayout.setVisibility(View.GONE);
                linearLayout1.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.GONE);

                TextView button = findViewById(R.id.complete_btn);
                button.setVisibility(View.VISIBLE);
                PushDownAnim.setPushDownAnimTo(button).setScale(0.8f);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showFinishing();
                    }
                });
            }
        });

        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(GameActivity.this, BlankActivity.class);
                intent.putExtra("target", "GameActivity");
                intent.putExtra("gameType", gameType);
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        returnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onBackPressed();
            }
        });
    }

}

