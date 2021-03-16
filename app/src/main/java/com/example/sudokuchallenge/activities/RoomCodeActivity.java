package com.example.sudokuchallenge.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.example.sudokuchallenge.R;
import com.example.sudokuchallenge.models.Room;
import com.example.sudokuchallenge.models.RoomQueues;
import com.example.sudokuchallenge.utils.SudokuMaker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    private boolean loadingAlreadyStarted;

    private Handler handler;
    private Runnable runnable;

    private MediaPlayer northPlayer;
    private MediaPlayer naturePlayer;

    private SoundPool soundPool;
    private int buttonSound;

    SudokuMaker sudokuMaker;

    private static final int MAX_VOLUME = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_code);

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
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION) //you can just press ctrl+b after clicking on USAGE_, and it will take you to a new java file where you can see descriptions of all these usage constants
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) //same here as above, press ctrl+b
                    .build();

            soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        } else{
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        buttonSound = soundPool.load(this, R.raw.button_click, 1);//priority doesn't has any effect here, but it's recommended to pass a 1 here for future compatibility
        //oh! and don't forget to override onDestroy to release the soundPool object



        loadingAlreadyStarted = false;

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

        TextView easyView = difficultyDialog.findViewById(R.id.easy_difficulty_view);
        TextView mediumView = difficultyDialog.findViewById(R.id.medium_difficulty_view);
        TextView difficultView = difficultyDialog.findViewById(R.id.difficult_difficulty_view);
        TextView timeAttackView = difficultyDialog.findViewById(R.id.time_attack_difficulty_view);
        TextView timeAttackBorder = difficultyDialog.findViewById(R.id.time_attack_border);
        timeAttackBorder.setVisibility(View.INVISIBLE);
        timeAttackView.setVisibility(View.INVISIBLE);

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

        difficultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                difficulty = SudokuMaker.DIFFICULT;
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
                //ToDo: Show Error
            }
        });

        boolean codePresent = false;
        for(int i = 0; i<roomQueuesList.size(); i++){
            if(code.equals(roomQueuesList.get(i).getRoomCode())){
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
            }
        });
        createRoomDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                createRoomDialogOn = false;
                roomQueueReference.removeValue();
            }
        });

        String code;
        while (true){
            code = generateRoomCode();
            if(postCodeToCloud(code))
                break;
        }
        createRoomDialog.show();

        LinearLayout createRoomView = createRoomDialog.findViewById(R.id.create_room_view);
        createRoomView.setVisibility(View.VISIBLE);

        TextView codeView = createRoomDialog.findViewById(R.id.room_code);
        codeView.setText(code);
        LinearLayout backImage = createRoomDialog.findViewById(R.id.room_code_dialog_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRoomDialog.dismiss();
            }
        });

        String[] codeArr = new String[1];
        codeArr[0] = code;
        database.getReference().child("Rooms").child(code).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Room room = snapshot.getValue(Room.class);
                    Intent intent = new Intent(RoomCodeActivity.this, PlayWithFriendActivity.class);
                    intent.putExtra("role", "creator");
                    intent.putExtra("difficulty", difficulty);
                    intent.putExtra("code", codeArr[0]);
                    intent.putExtra("roomId", codeArr[0]);
                    intent.putExtra("sudokuMaker", sudokuMaker);
                    if(room.creatorHasLoaded && room.sidekickHasLoaded){
                        // send intent
                        Log.d("TAG", "Loaded both");
                        database.getReference().child("Rooms").child(codeArr[0]).removeEventListener(this);
                        createRoomDialog.dismiss();
                        startActivity(intent);
                        finish();
                    } else if(room.creatorHasLoaded && !room.sidekickHasLoaded){
                        // wait/do nothing
                        Log.d("TAG", "Loaded creator not sidekick");
                    } else if(!room.creatorHasLoaded){
                        // load sudoku
                        if(!loadingAlreadyStarted) {
                            Log.d("TAG", "Loading creator");
                            new StartFriendActivityTask(RoomCodeActivity.this, sudokuMaker).execute(intent);
                            loadingAlreadyStarted = true;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showJoinRoomDialog(){
        joinRoomDialog = new Dialog(RoomCodeActivity.this);
        joinRoomDialog.setContentView(R.layout.room_code_dialog);
        joinRoomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        joinRoomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                    loading.setVisibility(View.VISIBLE);
                    database.getReference().child("RoomQueue").child(code).addListenerForSingleValueEvent(new ValueEventListener() {
                        boolean roomExists = false;

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
//                                joinRoomDialog.dismiss();
                                RoomQueues roomQueues = snapshot.getValue(RoomQueues.class);
                                roomExists = true;

                                Room room1 = new Room();
                                room1.setRoomCode(code);
                                room1.setRoomId(code);
                                room1.setDifficulty(roomQueues.getDifficulty());
                                database.getReference().child("Rooms").child(code).setValue(room1);


                                database.getReference().child("Rooms").child(code).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            Room room = snapshot.getValue(Room.class);
                                            Intent intent = new Intent(RoomCodeActivity.this, PlayWithFriendActivity.class);
                                            intent.putExtra("code", code);
                                            intent.putExtra("roomId", code);
                                            intent.putExtra("role", "sidekick");
//                                            intent.putExtra("room", room);
                                            intent.putExtra("difficulty", roomQueues.getDifficulty());
                                            intent.putExtra("sudokuMaker", sudokuMaker);
                                            if (room.sidekickHasLoaded && room.creatorHasLoaded) {
                                                // send intent
                                                Log.d("TAG", "Loaded both");
                                                database.getReference().child("Rooms").child(code).removeEventListener(this);
                                                joinRoomDialog.dismiss();
                                                startActivity(intent);
                                                finish();
                                            } else if (room.sidekickHasLoaded && !room.creatorHasLoaded) {
                                                // wait/do nothing
                                                Log.d("TAG", "Loaded sidekick not creator");
                                            } else if (!room.sidekickHasLoaded) {
                                                // load sudoku
                                                if (!loadingAlreadyStarted) {
                                                    Log.d("TAG", "Loading sidekick");
                                                    new StartFriendActivityTask(RoomCodeActivity.this, sudokuMaker).execute(intent);
                                                    loadingAlreadyStarted = true;
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            } else {
                                loading.setVisibility(View.GONE);
                                Toast.makeText(RoomCodeActivity.this, "Room doesn't exists", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

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
}

class StartFriendActivityTask extends AsyncTask<Intent, Void, Void> {
    private Context context;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    int difficulty;
    String role;
    String code;
    String roomId;
    Intent intent;
    SudokuMaker sudokuMaker;
    public StartFriendActivityTask(Context context, SudokuMaker sudokuMaker){

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        this.context = context;
        this.sudokuMaker = sudokuMaker;
    }


    @Override
    protected Void doInBackground(Intent... intents) {
        intent = intents[0];
        difficulty = intents[0].getIntExtra("difficulty", SudokuMaker.MEDIUM);
        role = intents[0].getStringExtra("role");
        code = intents[0].getStringExtra("code");
        roomId = intents[0].getStringExtra("roomId");
        sudokuMaker.setDifficulty(difficulty);
        Log.d("madeSudoku", "made sudoku");
        return null;
    }

    @Override
    protected void onPostExecute(Void sudokuMaker) {
        if(role.equals("creator")){
            database.getReference().child("Rooms").child(roomId).child("creatorHasLoaded").setValue(true);
        } else{
            database.getReference().child("Rooms").child(roomId).child("sidekickHasLoaded").setValue(true);
        }
        Log.d("values",role + ": done");
    }


}