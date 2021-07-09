package com.strangelet.sudokuchallenge.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import com.strangelet.sudokuchallenge.R;
import com.strangelet.sudokuchallenge.models.ProjectedUser;
import com.strangelet.sudokuchallenge.models.RoomQueues;
import com.strangelet.sudokuchallenge.models.User;
import com.strangelet.sudokuchallenge.utils.SudokuMaker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.Random;

public class RoomCodeActivity extends AppCompatActivity{

    FirebaseAuth auth;
    FirebaseDatabase database;

    private TextView createRoomBtn;
    private TextView joinRoomBtn;
    private ImageView backButton;

    private int difficulty;

    private boolean dialogIsOpen;

    private Dialog difficultyDialog;
    private boolean difficultyDialogOn;
    private Dialog createRoomDialog;
    private boolean createRoomDialogOn;
    private Dialog joinRoomDialog;
    private boolean joinRoomDialogOn;

    private DatabaseReference roomQueueReference;

    private boolean creatorHasLoaded;
    private boolean sidekickHasLoaded;

    private int naturePosition;
    private int northernPosition;

    private boolean createRoomDialogOpen;

    private boolean loadingAlreadyStarted;
    private boolean roomIsLoading;

    private Handler handler;
    private Runnable runnable;

    private MediaPlayer northPlayer;
    private MediaPlayer naturePlayer;

    private SoundPool soundPool;
    private int buttonSound;

    SudokuMaker sudokuMaker;

    private boolean sendingIntent;
    private Intent intent;
    private boolean stopped;

    private String roomCode;
    private boolean joinedRoom;

    private static final int MAX_VOLUME = 100;
    private boolean sidekickHasEntered;
    private ValueEventListener listener;

    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_code);

        roomCode = null;
        dialogIsOpen = false;
        roomIsLoading = false;
        createRoomDialogOpen = false;
        sidekickHasEntered = false;

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if(!isNetworkAvailable()) {
                    setNetworkError();
                } else
                    handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION) //you can just press ctrl+b after clicking on USAGE_, and it will take you to a new java file where you can see descriptions of all these usage constants
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) //same here as above, press ctrl+b
                    .build();

            soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        } else{
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        if(sfxEnabled())
            buttonSound = soundPool.load(this, R.raw.button_click, 1);//priority doesn't has any effect here, but it's recommended to pass a 1 here for future compatibility
        //oh! and don't forget to override onDestroy to release the soundPool object
        else
            buttonSound = 0;


        sendingIntent = false;
        loadingAlreadyStarted = false;
        stopped = false;
        joinedRoom = false;

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        sudokuMaker = new SudokuMaker();

        createRoomBtn = findViewById(R.id.create_room_btn);
        joinRoomBtn = findViewById(R.id.join_room_btn);
        backButton = findViewById(R.id.room_code_back);

        PushDownAnim.setPushDownAnimTo(createRoomBtn, joinRoomBtn, backButton).setScale(0.8f);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(buttonSound,1 , 1, 1, 0, 1);
                onBackPressed();
            }
        });

        createRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(buttonSound,1 , 1, 1, 0, 1);
                showDifficultySelectorDialog();
            }
        });
        joinRoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(buttonSound,1 , 1, 1, 0, 1);
                showJoinRoomDialog();
            }
        });

    }

    private void showDifficultySelectorDialog(){
        difficultyDialog = new Dialog(RoomCodeActivity.this);
        difficultyDialog.setContentView(R.layout.difficulty_layout);
        difficultyDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        difficultyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        difficultyDialog.getWindow().setGravity(Gravity.BOTTOM); // to attach the dialog to bottom
        difficultyDialog.getWindow().setWindowAnimations(R.style.DifficultyDialogAnimation);
        difficultyDialog.setCancelable(true);
        difficultyDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                difficultyDialogOn = true;
            }
        });
        difficultyDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                difficultyDialogOn = false;
            }
        });
        difficultyDialog.show();

        TextView beginnerView = difficultyDialog.findViewById(R.id.beginner_difficulty_view);
        TextView easyView = difficultyDialog.findViewById(R.id.easy_difficulty_view);
        TextView mediumView = difficultyDialog.findViewById(R.id.medium_difficulty_view);
        TextView trickyView = difficultyDialog.findViewById(R.id.tricky_difficulty_view);
        TextView fiendishView = difficultyDialog.findViewById(R.id.fiendish_difficulty_view);
        TextView diabolicalView = difficultyDialog.findViewById(R.id.diabolical_difficulty_view);
        TextView timeAttackView = difficultyDialog.findViewById(R.id.time_attack_difficulty_view);
        timeAttackView.setVisibility(View.INVISIBLE);


        beginnerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficulty = SudokuMaker.BEGINNER;
                difficultyDialog.dismiss();
                showCreateRoomDialog();
            }
        });

        easyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficulty = SudokuMaker.EASY;
                difficultyDialog.dismiss();
                showCreateRoomDialog();
            }
        });

        mediumView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficulty = SudokuMaker.MEDIUM;
                difficultyDialog.dismiss();
                showCreateRoomDialog();
            }
        });

        trickyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficulty = SudokuMaker.TRICKY;
                difficultyDialog.dismiss();
                showCreateRoomDialog();
            }
        });

        fiendishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficulty = SudokuMaker.FIENDISH;
                difficultyDialog.dismiss();
                showCreateRoomDialog();
            }
        });

        diabolicalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficulty = SudokuMaker.DIABOLICAL;
                difficultyDialog.dismiss();
                showCreateRoomDialog();
            }
        });
    }

    private void setNetworkError(){
        Dialog networkLostDialog = new Dialog(this);
        networkLostDialog.setContentView(R.layout.notice_dialog);
        networkLostDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        networkLostDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView networkLostNote = networkLostDialog.findViewById(R.id.confirmation_question);
        networkLostNote.setText("Network Error");
        networkLostNote.setGravity(Gravity.CENTER);
        TextView yes = networkLostDialog.findViewById(R.id.confirmation_yes);
        TextView no = networkLostDialog.findViewById(R.id.confirmation_no);
        yes.setVisibility(View.GONE);
        no.setVisibility(View.GONE);
        networkLostDialog.show();
        networkLostDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onBackPressed();
            }
        });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String generateRoomCode(){
        String[] arr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<6; i++){
            int index = random.nextInt(26);
            sb.append(arr[index]);
        }
        return sb.toString();
    }
    private boolean postCodeToCloud(String code){
        String id = auth.getCurrentUser().getUid();
        RoomQueues roomQueue = new RoomQueues(code, id, difficulty);
        roomQueue.setTimestamp(System.currentTimeMillis());
        ArrayList<RoomQueues> roomQueuesList= new ArrayList<>();

        database.getReference().child("RoomQueue").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomQueuesList.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    RoomQueues roomQueues1 = snapshot1.getValue(RoomQueues.class);
                    roomQueuesList.add(roomQueues1);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        boolean codePresent = false;
        for(int i = 0; i<roomQueuesList.size(); i++){
            if(roomQueuesList.size()!= 0 && code.equals(roomQueuesList.get(i).getRoomCode())){
                if((System.currentTimeMillis()-roomQueuesList.get(i).getTimestamp())<86400000)
                    codePresent = true;
                break;
            }
        }

        if(codePresent){
            return false;
        } else{
            roomQueueReference = database.getReference().child("RoomQueue").child(code);
            roomQueueReference.setValue(roomQueue);
            return true;
        }
    }
    private void showCreateRoomDialog(){
        createRoomDialog = new Dialog(RoomCodeActivity.this);
        createRoomDialog.setContentView(R.layout.room_code_dialog);
        createRoomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        createRoomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        createRoomDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                createRoomDialogOn = true;
                createRoomDialogOpen = true;
            }
        });
        String code;
        while (true){
            code = generateRoomCode();
            if(postCodeToCloud(code))
                break;
        }
        String finalCode = code;
        createRoomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                loadingAlreadyStarted = false;
                createRoomDialogOn = false;
                createRoomDialogOpen = false;
                if(finalCode !=null && listener!=null) {
                    database.getReference().child("RoomQueue").child(finalCode).removeEventListener(listener);
                    roomCode = null;
                    listener = null;
                }
                if(!joinedRoom){
                    database.getReference().child("RoomQueue").child(finalCode).removeValue();
                }
            }
        });
        createRoomDialog.show();

        LinearLayout createRoomView = createRoomDialog.findViewById(R.id.create_room_view);
        createRoomView.setVisibility(View.VISIBLE);

        TextView codeView = createRoomDialog.findViewById(R.id.room_code);
        codeView.setText(code);
//        roomCode = code;
        LinearLayout backImage = createRoomDialog.findViewById(R.id.room_code_dialog_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoomDialog.dismiss();
            }
        });

        String[] codeArr = new String[1];
        codeArr[0] = code;
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    RoomQueues roomQueues = snapshot.getValue(RoomQueues.class);
                    if (roomQueues.getSidekickId() != null) {
                        intent = new Intent(RoomCodeActivity.this, PlayWithFriendActivity.class);
                        intent.putExtra("role", "creator");
                        intent.putExtra("difficulty", difficulty);
                        intent.putExtra("code", codeArr[0]);
                        intent.putExtra("sudokuMaker", sudokuMaker);
                        if (roomQueues.creatorHasLoaded && roomQueues.sidekickHasLoaded) {
                            // send intent
                            SharedPreferences sharedPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
                            String jsonUser = sharedPreferences.getString(SigningActivity.USER_KEY, null);
                            User user = new Gson().fromJson(jsonUser, User.class);
                            sendingIntent = true;
                            database.getReference().child("Rooms").child(codeArr[0]).child("creatorSudoku").setValue(sudokuMaker.getOpponentSudoku());
                            ProjectedUser projectedUser = new ProjectedUser(user.getUid(), user.getNickName());
                            database.getReference().child("Rooms").child(codeArr[0]).child("creator").setValue(projectedUser);
                            joinedRoom = true;
                            Log.d("TAG", "Loaded both");
                            database.getReference().child("Rooms").child(codeArr[0]).removeEventListener(this);
                            createRoomDialog.dismiss();
                            if (!stopped) {
                                startActivity(intent);
                                finish();
                            }
                        } else if (roomQueues.creatorHasLoaded && !roomQueues.sidekickHasLoaded) {
                            // wait/do nothing
                            Log.d("TAG", "Loaded creator not sidekick");
                        } else if (!roomQueues.creatorHasLoaded) {
                            // load sudoku
                            if (!loadingAlreadyStarted) {
                                Log.d("TAG", "Loading creator");
                                SharedPreferences sharedPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
                                String jsonUser = sharedPreferences.getString(SigningActivity.USER_KEY, null);
                                User user = new Gson().fromJson(jsonUser, User.class);
                                new StartFriendActivityTask(RoomCodeActivity.this, sudokuMaker, user).execute(intent);
                                loadingAlreadyStarted = true;
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        database.getReference().child("RoomQueue").child(code).addValueEventListener(listener);
    }

    private void showJoinRoomDialog(){
        joinRoomDialog = new Dialog(RoomCodeActivity.this);
        joinRoomDialog.setContentView(R.layout.room_code_dialog);
        joinRoomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        joinRoomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        joinRoomDialog.setCancelable(true);
        joinRoomDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                joinRoomDialogOn = true;
            }
        });
        joinRoomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                joinRoomDialogOn = false;
                sidekickHasEntered = false;
                loadingAlreadyStarted = false;
                if(roomCode!=null && listener!=null) {
                    database.getReference().child("RoomQueue").child(roomCode).removeEventListener(listener);
                    roomCode = null;
                    listener = null;
                }
                if(timeoutHandler!=null && timeoutRunnable!= null) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    timeoutRunnable = null;
                    timeoutHandler = null;
                }
            }
        });
        joinRoomDialog.show();

        CardView joinRoomView = joinRoomDialog.findViewById(R.id.join_room_view);
        joinRoomView.setVisibility(View.VISIBLE);

        LinearLayout backImage = joinRoomDialog.findViewById(R.id.room_code_dialog_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    joinRoomDialog.dismiss();
            }
        });

        EditText roomCodeText = joinRoomDialog.findViewById(R.id.room_code_text);
        RelativeLayout submitCodeBtn = joinRoomDialog.findViewById(R.id.submit_room_code);
        submitCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LottieAnimationView loading = joinRoomDialog.findViewById(R.id.room_loading);
                String code = roomCodeText.getText().toString().trim();
                ArrayList<RoomQueues> roomQueuesArrayList = new ArrayList<>();
                if(code.length()==6) {
                    roomCode = code;
                    loading.setVisibility(View.VISIBLE);
                    listener = new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                RoomQueues roomQueues = snapshot.getValue(RoomQueues.class);
                                if(roomQueues.getSidekickId()==null || sidekickHasEntered) {
                                    sidekickHasEntered = true;
                                    database.getReference().child("RoomQueue").child(code).child("sidekickId").setValue(auth.getCurrentUser().getUid());
                                    dialogIsOpen = true;
                                    intent = new Intent(RoomCodeActivity.this, PlayWithFriendActivity.class);
                                    intent.putExtra("code", code);
                                    intent.putExtra("role", "sidekick");
                                    intent.putExtra("difficulty", roomQueues.getDifficulty());
                                    intent.putExtra("sudokuMaker", sudokuMaker);
                                    if (roomQueues.sidekickHasLoaded && roomQueues.creatorHasLoaded) {
                                        // send intent
                                        SharedPreferences sharedPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
                                        String jsonUser = sharedPreferences.getString(SigningActivity.USER_KEY, null);
                                        User user = new Gson().fromJson(jsonUser, User.class);
                                        database.getReference().child("Rooms").child(code).child("sidekickSudoku").setValue(sudokuMaker.getOpponentSudoku());
                                        ProjectedUser projectedUser = new ProjectedUser(user.getUid(), user.getNickName());
                                        database.getReference().child("Rooms").child(code).child("sidekick").setValue(projectedUser);
                                        sendingIntent = true;
                                        Log.d("TAG", "Loaded both");
                                        database.getReference().child("Rooms").child(code).removeEventListener(this);
                                        joinRoomDialog.dismiss();
                                        if (!stopped) {
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else if (roomQueues.sidekickHasLoaded && !roomQueues.creatorHasLoaded) {
                                        //wait/do nothing
                                        Log.d("TAG", "Loaded sidekick not creator");
                                    } else if (!roomQueues.sidekickHasLoaded) {
                                        // load sudoku
                                        if (!loadingAlreadyStarted) {
                                            initTimeout();
                                            Log.d("TAG", "Loading sidekick");
                                            SharedPreferences sharedPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
                                            String jsonUser = sharedPreferences.getString(SigningActivity.USER_KEY, null);
                                            User user = new Gson().fromJson(jsonUser, User.class);
                                            new StartFriendActivityTask(RoomCodeActivity.this, sudokuMaker, user).execute(intent);
                                            loadingAlreadyStarted = true;
                                        }
                                    }
                                }

                                else{
                                    loading.setVisibility(View.GONE);
                                    Toast.makeText(RoomCodeActivity.this, "Room doesn't exists", Toast.LENGTH_SHORT).show();
                                    database.getReference().child("RoomQueue").child(code).removeEventListener(this);
                                }
                            } else {
                                loading.setVisibility(View.GONE);
                                Toast.makeText(RoomCodeActivity.this, "Room doesn't exists", Toast.LENGTH_SHORT).show();
                                database.getReference().child("RoomQueue").child(code).removeEventListener(this);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };

                    database.getReference().child("RoomQueue").child(code).addValueEventListener(listener);
                }


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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();

        handler.removeCallbacks(runnable);
        fadeStopMediaPlayer();

        if(createRoomDialogOn)
            createRoomDialog.dismiss();
        if (joinRoomDialogOn)
            joinRoomDialog.dismiss();
        if(difficultyDialogOn)
            difficultyDialog.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(musicEnabled()) {
            playBackgroundMusic();
            playNatureSounds();
        }
        if(stopped && sendingIntent){
            startActivity(intent);
            finish();
        }
        stopped = false;
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
        stopped = true;
    }

    private void initTimeout(){
        timeoutHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d("TIM", "runnable ran");
                    if(joinRoomDialogOn) {
                        Log.d("TIM", "dismissing");
                        Toast.makeText(RoomCodeActivity.this, "Loading timeout", Toast.LENGTH_SHORT).show();
                        database.getReference().child("RoomQueue").child(roomCode).removeValue();
//                        database.getReference().child("RoomQueue").child(roomCode).child("sidekickId").removeValue();
                        joinRoomDialog.dismiss();
                        showJoinRoomDialog();
                    }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 25000);
    }

    private boolean musicEnabled(){
        SharedPreferences musicPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
        return musicPreferences.getBoolean(SigningActivity.MUSIC_SETTINGS_KEY, false);
    }

    private boolean sfxEnabled(){
        SharedPreferences sfxPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);
        return sfxPreferences.getBoolean(SigningActivity.SFX_SETTINGS_KEY, false);
    }

}




class StartFriendActivityTask extends AsyncTask<Intent, Void, Void> {
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    int difficulty;
    String role;
    String code;
    Intent intent;
    SudokuMaker sudokuMaker;
    User user;
    public StartFriendActivityTask(Context context, SudokuMaker sudokuMaker, User user){

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        this.context = context;
        this.sudokuMaker = sudokuMaker;
        this.user = user;
    }


    @Override
    protected Void doInBackground(Intent... intents) {
        intent = intents[0];
        difficulty = intents[0].getIntExtra("difficulty", SudokuMaker.MEDIUM);
        role = intents[0].getStringExtra("role");
        code = intents[0].getStringExtra("code");
        sudokuMaker.setDifficulty(difficulty);
        Log.d("madeSudoku", "made sudoku");
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if(role.equals("creator")){
            database.getReference().child("RoomQueue").child(code).child("creatorHasLoaded").setValue(true);
        } else{
            database.getReference().child("RoomQueue").child(code).child("sidekickHasLoaded").setValue(true);
        }
        Log.d("values",role + ": done");
    }
}