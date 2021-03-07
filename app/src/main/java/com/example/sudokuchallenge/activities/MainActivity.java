package com.example.sudokuchallenge.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.sudokuchallenge.R;
import com.example.sudokuchallenge.models.User;
import com.example.sudokuchallenge.utils.SudokuMaker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //ToDo: make difficulty dialog disappear when user clicks outside it, and also fill the background of difficulty dialog

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private GoogleSignInClient client;

    private TextView newGameButton;
    private TextView resumeGameButton;
    private TextView playOnlineButton;
    private TextView playWithFriend;
    private TextView spaceView;
    private TextView spaceView2;

    private TextView easyTextView;
    private TextView mediumTextView;
    private TextView difficultTextView;
    private TextView timeAttackTextView;

    private RelativeLayout profileButton;
//    private TextView creditsView;
    private RelativeLayout dailyChallengeBtn;
    private RelativeLayout storeButton;
    private RelativeLayout signInButton;
    private RelativeLayout settingsButton;
//    private RelativeLayout dailyRewardButton;

    private Dialog signUpDialog;
    private Dialog signInDialog;

    private boolean signOrCreate = false;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        sharedPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);

        showBoardDrop(); // to show animation

        newGameButton = (TextView) findViewById(R.id.new_game_button);
        resumeGameButton = (TextView) findViewById(R.id.resume_game_button);
        playOnlineButton = (TextView) findViewById(R.id.play_online_btn);
        playWithFriend = (TextView) findViewById(R.id.play_with_friend_button);
        spaceView = (TextView) findViewById(R.id.space_view);
        spaceView2 = (TextView) findViewById(R.id.space_view2);

        profileButton = findViewById(R.id.profile_button);
//        creditsView = findViewById(R.id.credits_text_view);
        storeButton = findViewById(R.id.store_button);
        signInButton = findViewById(R.id.sign_in_button);
        settingsButton = findViewById(R.id.main_settings_button);
        dailyChallengeBtn = findViewById(R.id.daily_challenge_btn);

        playOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.notice_dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView comingSoon = dialog.findViewById(R.id.confirmation_question);
                comingSoon.setText("Coming Soon");
                comingSoon.setGravity(Gravity.CENTER);
                TextView yes = dialog.findViewById(R.id.confirmation_yes);
                TextView no = dialog.findViewById(R.id.confirmation_no);
                yes.setVisibility(View.GONE);
                no.setVisibility(View.GONE);
                dialog.show();
            }
        });

        ImageView dailyChallengeCalendar = findViewById(R.id.daily_challenge_calender);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.calendar_scaling);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.reset();
        dailyChallengeCalendar.clearAnimation();
        dailyChallengeCalendar.startAnimation(anim);

        if(auth.getCurrentUser()!=null){
            signInButton.setVisibility(View.GONE);
        }


        SharedPreferences sharedPreferences = getSharedPreferences(GameActivity.PREFERENCE_KEY, MODE_PRIVATE);
        String jsonMakerResponse = sharedPreferences.getString(GameActivity.JSON_MAKER_RESPONSE_KEY, "");
        boolean setConfirmationDialog = false;
        if(jsonMakerResponse.equals("")) {
            resumeGameButton.setVisibility(View.GONE);
            spaceView.setVisibility(View.VISIBLE);
            spaceView2.setVisibility(View.VISIBLE);
        } else {
            resumeGameButton.setVisibility(View.VISIBLE);
            spaceView.setVisibility(View.GONE);
            spaceView2.setVisibility(View.GONE);
            setConfirmationDialog = true;
        }

        PushDownAnim.setPushDownAnimTo(newGameButton, resumeGameButton, playOnlineButton, playWithFriend, profileButton, storeButton, signInButton, settingsButton, dailyChallengeBtn)
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

        playWithFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(auth.getCurrentUser()!=null){
                    if(!isNetworkAvailable()){
                        showNetworkErrorDialog();
                    }else {
                        Intent intent = new  Intent(MainActivity.this, RoomCodeActivity.class);
                        startActivity(intent);
                    }
                }else {
                    setSignInPrompt();
                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInDialog();
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileDialog();
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void setConfirmation() {
        Dialog confirmationDialog = new Dialog(MainActivity.this);
        confirmationDialog.setContentView(R.layout.notice_dialog);
        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        confirmationDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // to set the width and height of the view
        //ToDo: Show pop animation when showing this dialog
//        confirmationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        confirmationDialog.setCancelable(true);
        confirmationDialog.show();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_anim);
        animation.reset();
        animation.setInterpolator(new BounceInterpolator());

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

    private void setSignInPrompt(){
        Dialog signInPrompt = new Dialog(MainActivity.this);
        signInPrompt.setContentView(R.layout.notice_dialog);
        signInPrompt.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        signInPrompt.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // to set the width and height of the view
        TextView prompt = signInPrompt.findViewById(R.id.confirmation_question);
        prompt.setText("You need to be Signed In to use this feature");
        TextView signIn = signInPrompt.findViewById(R.id.confirmation_yes);
        TextView no = signInPrompt.findViewById(R.id.confirmation_no);
        signIn.setText(">Sign in");
        no.setVisibility(View.GONE);
        //ToDo: Show pop animation when showing this dialog
//        confirmationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//        signInPrompt.setCancelable(true);
        signInPrompt.setCanceledOnTouchOutside(true);
        signInPrompt.show();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignInDialog();
                signInPrompt.dismiss();
            }
        });
    }

    private void showSignInDialog(){
        if(!isNetworkAvailable()){
            showNetworkErrorDialog();
        } else{
            signInDialog = new Dialog(MainActivity.this);
            signInDialog.setContentView(R.layout.sign_in_dialog);
            signInDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
            signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // to set the width and height of the view
            signInDialog.setCancelable(true);
            signInDialog.show();

            LinearLayout back_btn = signInDialog.findViewById(R.id.sign_in_dialog_back);
            back_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signInDialog.dismiss();
                }
            });

            CardView signIn = signInDialog.findViewById(R.id.sign_in);
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!isNetworkAvailable()){
                        signInDialog.dismiss();
                        showNetworkErrorDialog();
                    } else{
                        EditText emailEdit = signInDialog.findViewById(R.id.email_edittext);
                        EditText passwordEdit = signInDialog.findViewById(R.id.password_edittext);
                        if(!emailEdit.getText().toString().trim().isEmpty() && !passwordEdit.getText().toString().isEmpty()){
                            LottieAnimationView signInLoading = signInDialog.findViewById(R.id.sign_in_loading);
                            signInLoading.setVisibility(View.VISIBLE);
                            AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
                            anim.setDuration(1000);
                            signInLoading.startAnimation(anim);
                            auth.signInWithEmailAndPassword(emailEdit.getText().toString(), passwordEdit.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                signInButton.setVisibility(View.GONE);
                                                Toast.makeText(MainActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                                                signInLoading.setVisibility(View.GONE);
                                                signInDialog.dismiss();
                                            } else {
                                                signInLoading.setVisibility(View.GONE);
                                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                }
            });

            TextView googleSignInBtn = signInDialog.findViewById(R.id.google_sign_in_btn);
            googleSignInBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isNetworkAvailable()) {
                        signOrCreate = false;
                        signInWithGoogle();
                    }else{
                        Toast.makeText(MainActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            TextView createAccBtn = signInDialog.findViewById(R.id.create_acc_btn);
            createAccBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signInDialog.dismiss();
                    signUpDialog = new Dialog(MainActivity.this);
                    signUpDialog.setContentView(R.layout.create_account_dialog);
                    signUpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
                    signUpDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // to set the width and height of the view
                    signUpDialog.setCancelable(true);
                    signUpDialog.show();

                    LinearLayout back_btn = signUpDialog.findViewById(R.id.create_account_dialog_back);
                    back_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signUpDialog.dismiss();
                        }
                    });

                    TextView googleSignUpBtn = signUpDialog.findViewById(R.id.google_create_acc_btn);
                    googleSignUpBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(isNetworkAvailable()) {
                                signOrCreate = true;
                                signInWithGoogle();
                            }else{
                                Toast.makeText(MainActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    CardView signUp = signUpDialog.findViewById(R.id.create_account);
                    signUp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!isNetworkAvailable()){
                                signUpDialog.dismiss();
                                showNetworkErrorDialog();
                            } else {
                                EditText emailEditText = signUpDialog.findViewById(R.id.create_email_edittext);
                                EditText passwordEditText = signUpDialog.findViewById(R.id.create_password_edittext);
                                EditText confirmPasswordEditText = signUpDialog.findViewById(R.id.create_confirm_password_edittext);

                                if (!emailEditText.getText().toString().trim().isEmpty() && !passwordEditText.getText().toString().isEmpty() && !confirmPasswordEditText.getText().toString().isEmpty()){
                                    if (passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
                                        LottieAnimationView signUpLoading = signUpDialog.findViewById(R.id.create_account_loading);
                                        signUpLoading.setVisibility(View.VISIBLE);
                                        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
                                        anim.setDuration(1000);
                                        signUpLoading.startAnimation(anim);
                                        signUpLoading.setVisibility(View.VISIBLE);
                                        signUpLoading.startAnimation(anim);
                                        auth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
//                                                            User currentUser = User.getCurrentUser();
                                                            String jsonUser = sharedPreferences.getString(SigningActivity.USER_KEY, null);
                                                            Gson gson = new Gson();
                                                            User currentUser = gson.fromJson(jsonUser, User.class);
                                                            currentUser.setEmail(emailEditText.getText().toString());
                                                            currentUser.setUid(task.getResult().getUser().getUid());
                                                            jsonUser = gson.toJson(currentUser);
                                                            editor = sharedPreferences.edit();
                                                            editor.putString(SigningActivity.USER_KEY, jsonUser);
                                                            editor.apply();

                                                            database.getReference().child("Users").child(currentUser.getUid()).setValue(currentUser);
                                                            Toast.makeText(MainActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                                            signInButton.setVisibility(View.GONE);
                                                            signUpLoading.setVisibility(View.GONE);
                                                            signUpDialog.dismiss();
                                                        } else {
                                                            signUpLoading.setVisibility(View.GONE);
                                                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(MainActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                                    }
                            }
                            }
                        }
                    });

                    TextView alreadyHaveAcc = signUpDialog.findViewById(R.id.already_have_acc);
                    alreadyHaveAcc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signUpDialog.dismiss();
                            signInDialog.show();
                        }
                    });
                }
            });
        }
    }

    private void showNetworkErrorDialog(){
        Dialog networkError = new Dialog(MainActivity.this);
        networkError.setContentView(R.layout.notice_dialog);
        networkError.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        networkError.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // to set the width and height of the view
        TextView prompt = networkError.findViewById(R.id.confirmation_question);
        prompt.setText("Make sure you have an active internet connection and try again later");
        TextView yes = networkError.findViewById(R.id.confirmation_yes);
        TextView no = networkError.findViewById(R.id.confirmation_no);
        yes.setVisibility(View.GONE);
        no.setVisibility(View.GONE);
        //ToDo: Show pop animation when showing this dialog
//        confirmationDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        networkError.setCancelable(true);
        networkError.show();
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
                difficultyLevelDialog.dismiss();
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
                difficultyLevelDialog.dismiss();
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
                difficultyLevelDialog.dismiss();
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
                difficultyLevelDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, BlankActivity.class);
                intent.putExtra("target", "GameActivity");
                intent.putExtra("gameType", "new game");
                intent.putExtra("difficulty", SudokuMaker.TIME_ATTACK);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
    }

    private int getDisplayHeight() {
        return this.getResources().getDisplayMetrics().heightPixels;
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //-------------------------------------Code for authentication with google---------------------------------------------
    private void signInWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        client = GoogleSignIn.getClient(this, gso);
        signIn();
    }

    int RC_SIGN_IN = 50;
    private void signIn() {
        Intent signInIntent = client.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                if(!signOrCreate)
                    firebaseSignInAuthWithGoogle(account.getIdToken());
                else
                    firebaseSignUpAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseSignInAuthWithGoogle(String idToken) {

        LottieAnimationView signInLoading = signInDialog.findViewById(R.id.sign_in_loading);
        signInLoading.setVisibility(View.VISIBLE);
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(1000);
        signInLoading.startAnimation(anim);
        signInLoading.setVisibility(View.VISIBLE);
        signInLoading.startAnimation(anim);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task){
                        if (task.isSuccessful()){
                            //add user details to database
                            Toast.makeText(MainActivity.this, "Sign In successful", Toast.LENGTH_SHORT).show();
                            ArrayList<User> usersList = new ArrayList<>();
                            database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    usersList.clear();
                                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                        User user = snapshot1.getValue(User.class);
                                        usersList.add(user);
                                    }
                                    boolean userExists = false;
                                    for(int i = 0; i<usersList.size(); i++){
                                        if(usersList.get(i).getUid().equals(auth.getCurrentUser().getUid())){
                                            userExists = true;
                                            break;
                                        }
                                    }

                                    if(!userExists){
                                        String jsonUser = sharedPreferences.getString(SigningActivity.USER_KEY, null);
                                        Gson gson = new Gson();
                                        User currentUser = gson.fromJson(jsonUser, User.class);
//                                        User currentUser = User.getCurrentUser();
                                        currentUser.setEmail(task.getResult().getUser().getEmail());
                                        currentUser.setUid(task.getResult().getUser().getUid());
                                        database.getReference().child("Users").child(currentUser.getUid()).setValue(currentUser);
                                        jsonUser = gson.toJson(currentUser);
                                        editor = sharedPreferences.edit();
                                        editor.putString(SigningActivity.USER_KEY, jsonUser);
                                        editor.apply();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                            signInButton.setVisibility(View.GONE);
                            signInLoading.setVisibility(View.GONE);
                            signInDialog.dismiss();
                        }else{
                            Toast.makeText(MainActivity.this, "Sign In unsuccessful", Toast.LENGTH_SHORT).show();
                            signInLoading.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void firebaseSignUpAuthWithGoogle(String idToken){

        LottieAnimationView signUpLoading = signUpDialog.findViewById(R.id.create_account_loading);
        signUpLoading.setVisibility(View.VISIBLE);
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(1000);
        signUpLoading.startAnimation(anim);
        signUpLoading.setVisibility(View.VISIBLE);
        signUpLoading.startAnimation(anim);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task){
                        if (task.isSuccessful()){
                            //add user details to database
                            ArrayList<User> usersList = new ArrayList<>();
                            Toast.makeText(MainActivity.this, "Sign In successful", Toast.LENGTH_SHORT).show();
                            database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    usersList.clear();
                                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                        User user = snapshot1.getValue(User.class);
                                        usersList.add(user);
                                    }
                                    boolean userExists = false;
                                    for(int i = 0; i<usersList.size(); i++){
                                        if(usersList.get(i).getUid().equals(auth.getCurrentUser().getUid())){
                                            userExists = true;
                                            break;
                                        }
                                    }

                                    if(!userExists){
                                        String jsonUser = sharedPreferences.getString(SigningActivity.USER_KEY, null);
                                        Gson gson = new Gson();
                                        User currentUser = gson.fromJson(jsonUser, User.class);
//                                        User currentUser = User.getCurrentUser();
                                        currentUser.setEmail(task.getResult().getUser().getEmail());
                                        currentUser.setUid(task.getResult().getUser().getUid());
                                        database.getReference().child("Users").child(currentUser.getUid()).setValue(currentUser);
                                        jsonUser = gson.toJson(currentUser);
                                        editor = sharedPreferences.edit();
                                        editor.putString(SigningActivity.USER_KEY, jsonUser);
                                        editor.apply();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                            signInButton.setVisibility(View.GONE);
                            signUpLoading.setVisibility(View.GONE);
                            signUpDialog.dismiss();
                        }else{
                            Toast.makeText(MainActivity.this, "Sign In unsuccessful", Toast.LENGTH_SHORT).show();
                            signUpLoading.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void showProfileDialog(){
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.profile_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();

        TextView nicknameTextView = dialog.findViewById(R.id.nickname_text_profile);
        ImageView displayPic = dialog.findViewById(R.id.display_pic);
        TextView emailProfile = dialog.findViewById(R.id.email_profile);

        TextView easySudokusPlayed = dialog.findViewById(R.id.easy_games_played);
        TextView easySudokusCompleted = dialog.findViewById(R.id.easy_games_completed);
        TextView easySudokuAvgTime = dialog.findViewById(R.id.easy_avg_time);
        TextView easySudokuBestTime = dialog.findViewById(R.id.easy_best_time);

        TextView mediumSudokusPlayed = dialog.findViewById(R.id.medium_games_played);
        TextView mediumSudokusCompleted = dialog.findViewById(R.id.medium_games_completed);
        TextView mediumSudokuAvgTime = dialog.findViewById(R.id.medium_avg_time);
        TextView mediumSudokuBestTime = dialog.findViewById(R.id.medium_best_time);

        TextView difficultSudokusPlayed = dialog.findViewById(R.id.difficult_games_played);
        TextView difficultSudokusCompleted = dialog.findViewById(R.id.difficult_games_completed);
        TextView difficultSudokuAvgTime = dialog.findViewById(R.id.difficult_avg_time);
        TextView difficultSudokuBestTime = dialog.findViewById(R.id.difficult_best_time);

    }
}
