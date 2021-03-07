package com.example.sudokuchallenge.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokuchallenge.R;
import com.example.sudokuchallenge.customViews.OpponentSudokuBoard;
import com.example.sudokuchallenge.customViews.SudokuBoard;
import com.example.sudokuchallenge.models.Room;
import com.example.sudokuchallenge.models.User;
import com.example.sudokuchallenge.utils.OpponentSudoku;
import com.example.sudokuchallenge.utils.SudokuMaker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.Locale;

public class PlayWithFriendActivity extends AppCompatActivity {

    //ToDo: We are not passing sudokumaker to firebase, so set sudokumaker to shared preferences since in case of network loss, we want user to start where it left
    //ToDo: listen for connectivity
    //ToDo: delete a room
    //ToDo: show loading, i.e. show blankActivity
    private Room room;
    private boolean isCreator;

    private TextView timeTextView;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference roomReference;

    private int difficulty;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private SudokuMaker sudokuMaker;
    private SudokuBoard sudokuBoard;
    private ArrayList<ArrayList<Integer>> opPartialBoard;
    private ArrayList<ArrayList<Integer>> opWorkingBoard;

    private Handler timeHandler;
    private Runnable timeRunner;
    private String time;

    private boolean opponentHasFinished = false;
    int rank = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_with_friend);

        //ToDo: Add code to Room
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        sharedPreferences = getSharedPreferences(SigningActivity.USER_DATA_PREFERENCE_KEY, MODE_PRIVATE);

        String jsonUser = sharedPreferences.getString(SigningActivity.USER_KEY, null);
        Gson gson = new Gson();
        User currentUser = gson.fromJson(jsonUser, User.class);
        Log.d("myUser", currentUser.getEmail());

        difficulty = getIntent().getIntExtra("difficulty", SudokuMaker.MEDIUM);
        sudokuBoard = (SudokuBoard) findViewById(R.id.sudoku_board_friend);
        sudokuBoard.setDifficulty(difficulty);
        sudokuMaker = sudokuBoard.getSudokuMaker();

        OpponentSudokuBoard opponentSudokuBoard = findViewById(R.id.opponent_sudoku);

        int[][] partialBoard = sudokuMaker.getPartialBoard();
        int[][] workingBoard = sudokuMaker.getWorkingBoard();

        opPartialBoard = new ArrayList<>();
        opWorkingBoard = new ArrayList<>();

        for(int i = 0; i<9; i++){
            ArrayList<Integer> arrayList = new ArrayList<>();
            for(int j = 0; j<9; j++){
                arrayList.add(partialBoard[i][j]);
            }
            opPartialBoard.add(arrayList);
            arrayList = new ArrayList<>();
            for(int j = 0; j<9; j++){
                arrayList.add(workingBoard[i][j]);
            }
            opWorkingBoard.add(arrayList);
        }


        OpponentSudoku opponentSudoku = new OpponentSudoku(opPartialBoard, opWorkingBoard);
        opponentSudokuBoard.setOpponentSudoku(opponentSudoku);

        if (getIntent().getStringExtra("role").equals("creator")) {
            room = (Room) getIntent().getSerializableExtra("room");
            roomReference = database.getReference().child("Rooms").child(room.getRoomId());
            room.setCreator(currentUser);
            room.setCreatorSudoku(opponentSudoku);
            roomReference.setValue(room);
            isCreator = true;
            roomReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        OpponentSudoku sidekickSudoku = snapshot.getValue(Room.class).getSidekickSudoku();
                        if(sidekickSudoku!=null) {
                            opponentHasFinished = sidekickSudoku.isFinished();
                            opponentSudokuBoard.setOpponentSudoku(sidekickSudoku);
                            opponentSudokuBoard.invalidate();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else if (getIntent().getStringExtra("role").equals("sidekick")) {
            room = new Room();
            room.setRoomCode(getIntent().getStringExtra("code"));
            String roomId = getIntent().getStringExtra("roomId");
            room.setRoomId(roomId);
            room.setSidekick(currentUser);
            room.setDifficulty(difficulty);
            room.setSidekickSudoku(opponentSudoku);
            roomReference = database.getReference().child("Rooms").child(roomId);
            roomReference.setValue(room);
            isCreator = false;
            ArrayList<OpponentSudoku> list = new ArrayList<>();
            roomReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        OpponentSudoku creatorSudoku = snapshot.getValue(Room.class).getCreatorSudoku();
                        if(creatorSudoku!=null) {
                            opponentHasFinished = creatorSudoku.isFinished();
                            opponentSudokuBoard.setOpponentSudoku(creatorSudoku);
                            opponentSudokuBoard.invalidate();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        ImageView backImage = (ImageView) findViewById(R.id.back_button_friend);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView settingsImage = findViewById(R.id.settings_button_friend);
        settingsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo: open settings dialog
            }
        });


        ImageView imageView1 = (ImageView) findViewById(R.id.imageView1_friend);
        ImageView imageView2 = (ImageView) findViewById(R.id.imageView2_friend);
        ImageView imageView3 = (ImageView) findViewById(R.id.imageView3_friend);
        ImageView imageView4 = (ImageView) findViewById(R.id.imageView4_friend);
        ImageView imageView5 = (ImageView) findViewById(R.id.imageView5_friend);
        ImageView imageView6 = (ImageView) findViewById(R.id.imageView6_friend);
        ImageView imageView7 = (ImageView) findViewById(R.id.imageView7_friend);
        ImageView imageView8 = (ImageView) findViewById(R.id.imageView8_friend);
        ImageView imageView9 = (ImageView) findViewById(R.id.imageView9_friend);
        ImageView resetImage = (ImageView) findViewById(R.id.reset_image_friend);
        ImageView undoImage = (ImageView) findViewById(R.id.undo_image_friend);
        ImageView redoImage = (ImageView) findViewById(R.id.redo_image_friend);

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(1);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(2);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(3);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(4);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(5);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(6);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(7);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        imageView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(8);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();

            }
        });
        imageView9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(9);
                sudokuBoard.invalidate();
                postToFirebase();
                if (checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    showFinishing();
            }
        });
        resetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(SudokuMaker.RESET);
                sudokuBoard.invalidate();
                postToFirebase();
            }
        });
        undoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(SudokuMaker.UNDO);
                sudokuBoard.invalidate();
                postToFirebase();
            }
        });
        redoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(SudokuMaker.REDO);
                sudokuBoard.invalidate();
                postToFirebase();
            }
        });

        PushDownAnim.setPushDownAnimTo(imageView1, imageView2, imageView3, imageView4, imageView5,
                imageView6, imageView7, imageView8, imageView9, undoImage, resetImage, redoImage, backImage).setScale(0.8f);

        timeTextView = (TextView) findViewById(R.id.time_view_friend);
        runStopwatch();
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
                }
            }
        };
        timeHandler.post(timeRunner);
    }

    private void postToFirebase(){
        if(isCreator) {
            roomReference.child("creatorSudoku").setValue(sudokuMaker.getOpponentSudoku());
        }
        else {
            roomReference.child("sidekickSudoku").setValue(sudokuMaker.getOpponentSudoku());
        }
    }

    private void showFinishing(){
        timeHandler.removeCallbacks(timeRunner);
        sudokuMaker.finished(true);
        postToFirebase();
        sudokuBoard.invalidate();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.end_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        dialog.show();

        ImageView complete = dialog.findViewById(R.id.complete);
        ImageView victory = dialog.findViewById(R.id.victory);
        ImageView defeat = dialog.findViewById(R.id.defeat);
        complete.setVisibility(View.INVISIBLE);

        if(opponentHasFinished) {
            defeat.setVisibility(View.VISIBLE);
            rank = 2;
        }else {
            victory.setVisibility(View.VISIBLE);
            rank = 1;
        }

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
                //check it
                LinearLayout linearLayout = findViewById(R.id.btn_row_1_friend);
                LinearLayout linearLayout1 = findViewById(R.id.btn_row_2_friend);
                LinearLayout linearLayout2 = findViewById(R.id.btn_row_3_friend);

                linearLayout.setVisibility(View.GONE);
                linearLayout1.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.GONE);

                TextView button = findViewById(R.id.complete_btn_friend);
                button.setVisibility(View.VISIBLE);
                PushDownAnim.setPushDownAnimTo(button).setScale(0.8f);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEndDialog();
                    }
                });
            }
        });

        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Play again

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

    private void showEndDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.end_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        dialog.show();

        ImageView complete = dialog.findViewById(R.id.complete);
        ImageView victory = dialog.findViewById(R.id.victory);
        ImageView defeat = dialog.findViewById(R.id.defeat);
        complete.setVisibility(View.INVISIBLE);

        if(rank==1){
            victory.setVisibility(View.VISIBLE);
        }else if(rank ==2){
            defeat.setVisibility(View.VISIBLE);
        }

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
                //check it
                LinearLayout linearLayout = findViewById(R.id.btn_row_1_friend);
                LinearLayout linearLayout1 = findViewById(R.id.btn_row_2_friend);
                LinearLayout linearLayout2 = findViewById(R.id.btn_row_3_friend);

                linearLayout.setVisibility(View.GONE);
                linearLayout1.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.GONE);

                TextView button = findViewById(R.id.complete_btn_friend);
                button.setVisibility(View.VISIBLE);
                PushDownAnim.setPushDownAnimTo(button).setScale(0.8f);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEndDialog();
                    }
                });
            }
        });

        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Play again

            }
        });

    }

}
