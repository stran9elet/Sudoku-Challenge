package com.strangelet.sudokuchallenge.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.strangelet.sudokuchallenge.R;
import com.strangelet.sudokuchallenge.customViews.SudokuBoard;
import com.strangelet.sudokuchallenge.models.User;
import com.strangelet.sudokuchallenge.utils.SudokuMaker;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.gson.Gson;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    public static final String PREFERENCE_KEY = "sudoku_objects";
    public static final String JSON_MAKER_RESPONSE_KEY = "jsonMakerResponse";
    public static final String DAILY_CHALLENGE_SUDOKU_MAKER = "dailyChallengeSudokuMaker";

    public static final String LAST_DAY_KEY = "last day";
    public static final String NEXT_DAY_KEY = "next day";
    public static final String STREAK_RESET_DAY = "day after tomorrow";

    private static final int MAX_VOLUME= 100;

    String jsonMakerResponse;

    private RewardedAd mRewardedAd;
    private final String TAG = "AdMob";

    SudokuMaker sudokuMaker;
    SudokuBoard sudokuBoard;
    private TextView timeTextView;
    private ImageView timeOutTextView;

    private String gameType;
    private int difficulty;
    private String time;
    private Handler timeHandler;
    private Runnable timeRunner;
    private boolean isDailyChallenge;

    private SharedPreferences userDataSharedPreferences;
    private User user;
    private SharedPreferences.Editor userDataEditor;

    private MediaPlayer player;
    private SoundPool soundPool;

    private int btnClickSound;
    private int loadedSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        MobileAds.initialize(this);

        //i don't think we need to initialize user poster here also
        initializeSoundPool();

        userDataSharedPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
        String jsonUser = userDataSharedPreferences.getString(SigningActivity.USER_KEY, null);
        Gson userGson = new Gson();
        user = userGson.fromJson(jsonUser, User.class);
        userDataEditor = userDataSharedPreferences.edit();

        isDailyChallenge = getIntent().getBooleanExtra("dailyChallenge", false);

        ImageView backImage = (ImageView) findViewById(R.id.back_button);
        timeTextView = (TextView) findViewById(R.id.time_view);
        timeOutTextView = (ImageView) findViewById(R.id.timeout_view);

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                onBackPressed();
            }
        });

        ImageView settingsImage = findViewById(R.id.settings_button);
        settingsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                Dialog settingsDialog = new Dialog(GameActivity.this);
                settingsDialog.setContentView(R.layout.settings_dialog);
                settingsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                settingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
                settingsDialog.show();

                LinearLayout getHintBtn = settingsDialog.findViewById(R.id.get_a_hint_btn);
                TextView restartGameBtn = settingsDialog.findViewById(R.id.restart_cur_game_btn);
                TextView startNewGameBtn = settingsDialog.findViewById(R.id.start_another_game_btn);

                ImageView musicBtn = settingsDialog.findViewById(R.id.music_button);
                ImageView sfxBtn = settingsDialog.findViewById(R.id.sound_button);

                SharedPreferences settingsPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
                boolean musicState = settingsPreferences.getBoolean(SigningActivity.MUSIC_SETTINGS_KEY, false);
                boolean sfxState = settingsPreferences.getBoolean(SigningActivity.SFX_SETTINGS_KEY, false);
                if(musicState)
                    musicBtn.setImageResource(R.drawable.music_button);
                else
                    musicBtn.setImageResource(R.drawable.music_button_cancel);

                if(sfxState)
                    sfxBtn.setImageResource(R.drawable.sound_button);
                else
                    sfxBtn.setImageResource(R.drawable.sound_button_cancel);

                PushDownAnim.setPushDownAnimTo(getHintBtn, restartGameBtn, startNewGameBtn, musicBtn, sfxBtn).setScale(0.8f);
                if(sudokuMaker.isFinished){
                    getHintBtn.setVisibility(View.INVISIBLE);
                }else {
                    getHintBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                            // a side case, what if the user used hint on his last move. in that case, no button was pressed, and we won't be able to know game has ended
                            if (mRewardedAd != null) {
                                Activity activityContext = GameActivity.this;
                                mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                        // Handle the reward.
                                        Log.d("TAG", "The user earned the reward.");
                                        int rewardAmount = rewardItem.getAmount();
                                        String rewardType = rewardItem.getType();
                                        grantHint();
                                        settingsDialog.dismiss();
                                    }
                                });
                                loadAd();
                            } else {
                                Log.d("TAG", "No ads to show");
                                Toast.makeText(GameActivity.this, "Ad not ready yet. Try again in a few seconds", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

                restartGameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                        settingsDialog.dismiss();

                        Dialog dialog = new Dialog(GameActivity.this);
                        dialog.setContentView(R.layout.notice_dialog);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
                        dialog.show();

                        TextView confirmationQues = dialog.findViewById(R.id.confirmation_question);
                        TextView confirmationYes = dialog.findViewById(R.id.confirmation_yes);
                        TextView confirmationNo = dialog.findViewById(R.id.confirmation_no);

                        confirmationQues.setText("Do you really want to reset your sudoku board?\n\nNote- You won't be able get your progress back if you reset the sudoku");
                        confirmationYes.setText(">Yes");
                        confirmationNo.setText(">No");
                        confirmationYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sudokuMaker.reset();
                                sudokuBoard.invalidate();
                                dialog.dismiss();
                            }
                        });
                        confirmationNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                settingsImage.performClick();
                            }
                        });
                    }
                });

                startNewGameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                        //show dialog asking to confirm
                        settingsDialog.dismiss();

                        Dialog dialog = new Dialog(GameActivity.this);
                        dialog.setContentView(R.layout.notice_dialog);
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
                        dialog.show();

                        TextView confirmationQues = dialog.findViewById(R.id.confirmation_question);
                        TextView confirmationYes = dialog.findViewById(R.id.confirmation_yes);
                        TextView confirmationNo = dialog.findViewById(R.id.confirmation_no);

                        confirmationQues.setText("You really want to start a new game?\n\nNote- All your progress in the current game will be lost.\nDo you still wish to continue?");
                        confirmationYes.setText(">Yes");
                        confirmationNo.setText(">No");
                        confirmationYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(GameActivity.this, BlankActivity.class);
                                intent.putExtra("target", "GameActivity");
                                intent.putExtra("gameType", "new game");
                                intent.putExtra("difficulty", difficulty);
                                dialog.dismiss();
                                startActivity(intent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }
                        });
                        confirmationNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                settingsImage.performClick();
                            }
                        });
                    }
                });

                musicBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                        SharedPreferences settingsPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
                        boolean musicState = settingsPreferences.getBoolean(SigningActivity.MUSIC_SETTINGS_KEY, false);
                        SharedPreferences.Editor settingsEditor = settingsPreferences.edit();
                        settingsEditor.putBoolean(SigningActivity.MUSIC_SETTINGS_KEY, !musicState);
                        settingsEditor.apply();

                        if(musicState && player!=null) {
                            stopBackgroundMusic();
                            musicBtn.setImageResource(R.drawable.music_button_cancel);
                        }
                        else {
                            startBackgroundMusic();
                            musicBtn.setImageResource(R.drawable.music_button);
                        }
                    }
                });

                sfxBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences settingsPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
                        boolean sfxState = settingsPreferences.getBoolean(SigningActivity.SFX_SETTINGS_KEY, false);
                        SharedPreferences.Editor settingsEditor = settingsPreferences.edit();
                        settingsEditor.putBoolean(SigningActivity.SFX_SETTINGS_KEY, !sfxState);
                        settingsEditor.apply();

                        if(sfxState) {
                            btnClickSound = 0;
                            sfxBtn.setImageResource(R.drawable.sound_button_cancel);
                        }
                        else {
                            btnClickSound = loadedSound;
                            sfxBtn.setImageResource(R.drawable.sound_button);
                        }
                        soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                    }
                });

            }
        });

        gameType = getIntent().getStringExtra("gameType");
        sudokuBoard = (SudokuBoard) findViewById(R.id.sudoku_board);

        if (gameType.equals("new game")) {
            difficulty = getIntent().getIntExtra("difficulty", SudokuMaker.MEDIUM);
            sudokuMaker = (SudokuMaker)  getIntent().getSerializableExtra("sudokuMaker");
            sudokuBoard.setSudokuMaker(sudokuMaker);
            if (difficulty == SudokuMaker.TIME_ATTACK) {
//                sudokuMaker.timeLimit = 30*60;
                sudokuMaker.timeLimit = 5;
            }
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String jsonMakerResponse = gson.toJson(sudokuMaker, SudokuMaker.class);
            if(isDailyChallenge){
                editor.putString(DAILY_CHALLENGE_SUDOKU_MAKER, jsonMakerResponse);
            }else{
                editor.putString(JSON_MAKER_RESPONSE_KEY, jsonMakerResponse);
                if(difficulty==SudokuMaker.EASY)
                    user.setEasyGamesPlayed(user.getEasyGamesPlayed() + 1);
                else if(difficulty==SudokuMaker.BEGINNER)
                    user.setBeginnerGamesPlayed(user.getBeginnerGamesPlayed() + 1);
                else if(difficulty==SudokuMaker.TRICKY)
                    user.setTrickyGamesPlayed(user.getTrickyGamesPlayed() + 1);
                else if(difficulty==SudokuMaker.FIENDISH)
                    user.setFiendishGamesPlayed(user.getFiendishGamesPlayed() + 1);
                else if(difficulty==SudokuMaker.MEDIUM)
                    user.setMediumGamesPlayed(user.getMediumGamesPlayed() + 1);
                else if(difficulty==SudokuMaker.DIABOLICAL)
                    user.setDiabolicalGamesPlayed(user.getDiabolicalGamesPlayed() + 1);
                else if(difficulty==SudokuMaker.TIME_ATTACK)
                    user.setTimeAttackGamesPlayed(user.getTimeAttackGamesPlayed() + 1);
            }
            editor.apply();
        }

        if (gameType.equals("resume game")) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
            String jsonMakerResponse;
            Gson gson = new Gson();
            if(isDailyChallenge){
                jsonMakerResponse = sharedPreferences.getString(DAILY_CHALLENGE_SUDOKU_MAKER, "");
            }else{
                jsonMakerResponse = sharedPreferences.getString(JSON_MAKER_RESPONSE_KEY, "");
            }
            sudokuMaker = gson.fromJson(jsonMakerResponse, SudokuMaker.class);
            sudokuBoard.isResumedActivity(true, sudokuMaker);
            if(!sudokuMaker.finishedWithinTime)
                showTimeout();
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
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(1);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(2);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(3);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(4);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(5);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(6);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(7);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(8);
                sudokuBoard.invalidate();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
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
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(SudokuMaker.RESET);
                sudokuBoard.invalidate();
            }
        });
        undoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(SudokuMaker.UNDO);
                sudokuBoard.invalidate();
            }
        });
        redoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                sudokuMaker.setNumberPosition(SudokuMaker.REDO);
                sudokuBoard.invalidate();
            }
        });

        PushDownAnim.setPushDownAnimTo(imageView1, imageView2, imageView3, imageView4, imageView5,
                imageView6, imageView7, imageView8, imageView9, undoImage, resetImage, redoImage, backImage, settingsImage).setScale(0.8f);

        runStopwatch();

        String userJson = userGson.toJson(user);
        userDataEditor.putString(SigningActivity.USER_KEY, userJson);
        userDataEditor.apply();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadAd();
            }
        }, 2000);
    }

    private void grantHint() {
        Toast.makeText(this, "Added a number to sudoku!", Toast.LENGTH_SHORT).show();
        Random random = new Random();
        ArrayList<Pair<Integer,Integer>> zeroList = new ArrayList<>();
        int[][] workingBoard = sudokuMaker.getWorkingBoard();
        int[][] fullBoard = sudokuMaker.getFullBoard();
        for(int i = 0; i<9; i++){
            for(int j = 0; j<9; j++){
                if(workingBoard[i][j] == 0){
                    Pair<Integer, Integer> pair = new Pair<>(i, j);
                    zeroList.add(pair);
                }
            }
        }
        int index = random.nextInt(zeroList.size());
        Pair<Integer, Integer> pair = zeroList.get(index);
        int row = pair.first;
        int col = pair.second;
        int value = fullBoard[row][col];
        sudokuMaker.setSelectedRow(row + 1);
        sudokuMaker.setSelectedColumn(col + 1);
        sudokuMaker.setNumberPosition(value);
        sudokuBoard.invalidate();
        if (checkSudoku(workingBoard,fullBoard))
            showFinishing();
    }

    @Override
    public void finish() {
        super.finish();
        fadeOutAudio();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopBackgroundMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!"".equals(jsonMakerResponse)){
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            jsonMakerResponse = gson.toJson(sudokuMaker, SudokuMaker.class);
            if (isDailyChallenge) {
                editor.putString(DAILY_CHALLENGE_SUDOKU_MAKER, jsonMakerResponse);
            } else {
                editor.putString(JSON_MAKER_RESPONSE_KEY, jsonMakerResponse);
            }
            editor.apply();
            }
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
                    sudokuMaker.finishedWithinTime = false;
                } else {
                    int hours = (sudokuMaker.timeLimit - sudokuMaker.secondsElapsed) / 3600;
                    int minutesLeft = ((sudokuMaker.timeLimit - sudokuMaker.secondsElapsed) % 3600) / 60;
                    int secondsLeft = (sudokuMaker.timeLimit - sudokuMaker.secondsElapsed) % 60;
                    String time = String.format(Locale.getDefault(), "%02d:%02d", minutesLeft, secondsLeft);
                    timeTextView.setText(time);
                    if (sudokuMaker.secondsElapsed == sudokuMaker.timeLimit) {
                        sudokuMaker.timeLimit = 0;
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
        timeOutTextView.setVisibility(View.VISIBLE);
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
        String jsonUser1 = userDataSharedPreferences.getString(SigningActivity.USER_KEY, null);
        Gson userGson = new Gson();
        User user = userGson.fromJson(jsonUser1, User.class);

        timeHandler.removeCallbacks(timeRunner);
        sudokuMaker.finished(true);
        sudokuBoard.invalidate();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.end_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        dialog.getWindow().setWindowAnimations(R.style.DifficultyDialogAnimation);
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
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
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
                        soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                        showEndDialog();
                    }
                });
            }
        });

        if(isDailyChallenge){
            playAgain.setVisibility(View.GONE);
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(DAILY_CHALLENGE_SUDOKU_MAKER, "");
            editor.apply();

            Calendar calendar = Calendar.getInstance();
            Date today = calendar.getTime();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date tomorrow = calendar.getTime();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date dayAfterTomorrow = calendar.getTime();

            DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

            String todayAsString = dateFormat.format(today);
            String tomorrowAsString = dateFormat.format(tomorrow);
            String dayAfterTomorrowAsString = dateFormat.format(dayAfterTomorrow);

            String expectedDate = userDataSharedPreferences.getString(NEXT_DAY_KEY, "");
            if(todayAsString.equals(expectedDate) || user.getDailyChallengeStreak()==0)
                user.setDailyChallengeStreak(user.getDailyChallengeStreak() + 1);
            user.setDailyChallengesCompleted(user.getDailyChallengesCompleted() + 1);
            SharedPreferences.Editor editor2 = userDataSharedPreferences.edit();
            Gson gson = new Gson();
            String jsonUser = gson.toJson(user);
            editor2.putString(SigningActivity.USER_KEY, jsonUser);
            editor2.apply();

            SharedPreferences.Editor editor1 = userDataSharedPreferences.edit();
            editor1.putString(LAST_DAY_KEY, todayAsString);
            editor1.putString(NEXT_DAY_KEY, tomorrowAsString);
            editor1.putString(STREAK_RESET_DAY, dayAfterTomorrowAsString);
            editor1.apply();

        } else{
            playAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
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

            if(difficulty==SudokuMaker.EASY)
                user.setEasyGamesCompleted(user.getEasyGamesCompleted() + 1);
            else if(difficulty==SudokuMaker.MEDIUM)
                user.setMediumGamesCompleted(user.getMediumGamesCompleted() + 1);
            else if(difficulty==SudokuMaker.DIABOLICAL)
                user.setDiabolicalGamesCompleted(user.getDiabolicalGamesCompleted() + 1);
            else if(difficulty==SudokuMaker.BEGINNER)
                user.setBeginnerGamesCompleted(user.getBeginnerGamesCompleted() + 1);
            else if(difficulty==SudokuMaker.TRICKY)
                user.setTrickyGamesCompleted(user.getTrickyGamesCompleted() + 1);
            else if(difficulty==SudokuMaker.FIENDISH)
                user.setFiendishGamesCompleted(user.getFiendishGamesCompleted() + 1);
            else if(difficulty==SudokuMaker.TIME_ATTACK)
                if(sudokuMaker.finishedWithinTime)
                    user.setTimeAttackGamesCompleted(user.getTimeAttackGamesCompleted() + 1);

            SharedPreferences sharedPreferences1 = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor3 = sharedPreferences1.edit();
            editor3.putString(JSON_MAKER_RESPONSE_KEY, "");
            editor3.apply();
        }

        jsonMakerResponse = "";
        returnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                dialog.dismiss();
                onBackPressed();
            }
        });
    }

    private void showEndDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.end_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        dialog.getWindow().setWindowAnimations(R.style.DifficultyDialogAnimation);
        dialog.show();

        ImageView complete = dialog.findViewById(R.id.complete);

        ImageView viewGame = dialog.findViewById(R.id.end_back_btn);
        TextView playAgain = dialog.findViewById(R.id.end_play_again);
        TextView returnHome = dialog.findViewById(R.id.end_return_home);
        TextView timeTaken = dialog.findViewById(R.id.end_time_taken);
        timeTaken.setText("Time taken: " + time);

        PushDownAnim.setPushDownAnimTo(viewGame, playAgain, returnHome).setScale(0.8f);

        viewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                dialog.dismiss();
                //check it
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
                        soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                        showEndDialog();
                    }
                });
            }
        });

        if(isDailyChallenge){
            playAgain.setVisibility(View.GONE);
        }else {
            playAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
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
        }

        returnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(btnClickSound, 1, 1, 0, 0, 1);
                dialog.dismiss();
                onBackPressed();
            }
        });
    }


    private void startBackgroundMusic(){
        if(player == null)
            player = MediaPlayer.create(this, R.raw.winter_afternoon);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                startBackgroundMusic();
            }
        });

        player.start();
    }

    private void stopBackgroundMusic(){
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(musicEnabled())
            startBackgroundMusic();
    }

    private void setCurrentVolume(int currVolume){
        if(player!=null) {
            final float volume = (float) (1 - (Math.log(MAX_VOLUME - currVolume) / Math.log(MAX_VOLUME)));
            player.setVolume(volume, volume); //set volume takes two paramater
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
                    if(player!=null) {
                        player.stop();
                        player.release();
                        player = null;
                    }
                }
            }
        };
        handler.post(runnable);
    }

    private void initializeSoundPool(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION) //you can just press ctrl+b after clicking on USAGE_, and it will take you to a new java file where you can see descriptions of all these usage constants
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) //same here as above, press ctrl+b
                    .build();

            soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        } else{
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        loadedSound = soundPool.load(this, R.raw.button_click, 1);//priority doesn't has any effect here, but it's recommended to pass a 1 here for future compatibility
        //oh! and don't forget to override onDestroy to release the soundPool object

        if(sfxEnabled())
            btnClickSound = loadedSound;
        else
            btnClickSound = 0;
    }

    private boolean musicEnabled(){
        SharedPreferences musicPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
        return musicPreferences.getBoolean(SigningActivity.MUSIC_SETTINGS_KEY, false);
    }

    private boolean sfxEnabled(){
        SharedPreferences sfxPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
        return sfxPreferences.getBoolean(SigningActivity.SFX_SETTINGS_KEY, false);
    }

    private void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        //ToDo: change this to real app id
        // sample id is "ca-app-pub-3940256099942544/5224354917"
        // your id is "ca-app-pub-6250842590816182/2073298641"
        RewardedAd.load(this, "ca-app-pub-6250842590816182/2073298641",
                adRequest, new RewardedAdLoadCallback(){
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "ad loaded");
                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad was shown.");
                                mRewardedAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d(TAG, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Don't forget to set the ad reference to null so you
                                // don't show the ad a second time.
                                Log.d(TAG, "Ad was dismissed.");
                            }
                        });

                    }
                });
    }

}

