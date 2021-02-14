package com.example.sudokuchallenge.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sudokuchallenge.Classes.SudokuBoard;
import com.example.sudokuchallenge.Classes.SudokuMaker;
import com.example.sudokuchallenge.R;
import com.google.gson.Gson;
import com.thekhaeng.pushdownanim.PushDownAnim;

public class GameActivity extends AppCompatActivity {

    public static final String PREFERENCE_KEY = "sudoku_objects";
    public static final String JSON_MAKER_RESPONSE_KEY = "jsonMakerResponse";

    SudokuMaker sudokuMaker;
    SudokuBoard sudokuBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        String gameType = getIntent().getStringExtra("gameType");
        sudokuBoard = (SudokuBoard) findViewById(R.id.sudoku_board);

        if(gameType.equals("new game")) {
            int difficulty = getIntent().getIntExtra("difficulty", SudokuMaker.EASY);
            sudokuBoard.setDifficulty(difficulty);
            sudokuMaker = sudokuBoard.getSudokuMaker();
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String jsonMakerResponse = gson.toJson(sudokuMaker, SudokuMaker.class);
            editor.putString(JSON_MAKER_RESPONSE_KEY, jsonMakerResponse);
            editor.apply();
        }

        if(gameType.equals("resume game")){
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
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(2);
                sudokuBoard.invalidate();
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(3);
                sudokuBoard.invalidate();
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });
        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(4);
                sudokuBoard.invalidate();
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });
        imageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(5);
                sudokuBoard.invalidate();
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });
        imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(6);
                sudokuBoard.invalidate();
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });
        imageView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(7);
                sudokuBoard.invalidate();
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
            }
        });
        imageView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(8);
                sudokuBoard.invalidate();
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();

            }
        });
        imageView9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuMaker.setNumberPosition(9);
                sudokuBoard.invalidate();
                if(checkSudoku(sudokuMaker.getWorkingBoard(), sudokuMaker.getFullBoard()))
                    Toast.makeText(GameActivity.this, "Complete!", Toast.LENGTH_SHORT).show();
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
                imageView6, imageView7, imageView8, imageView9, undoImage, resetImage, redoImage).setScale(0.8f);


    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//    }
// we need to add the sudokuMaker to onStop and not onDestroy because onstop is called before the other activity is opened,
//whereas onDestroy is called after the MainActivity's onCreate()
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonMakerResponse = gson.toJson(sudokuMaker, SudokuMaker.class);
        editor.putString(JSON_MAKER_RESPONSE_KEY, jsonMakerResponse);
        editor.apply();
        finish();
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, BlankActivity.class);
        intent.putExtra("target", "MainActivity");
        startActivity(intent);
//        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private boolean checkSudoku(int[][] workingArray, int[][] fullArray){
        for(int i = 0; i<fullArray.length; i++){
            for(int j = 0; j<fullArray[i].length; j++){
                if(fullArray[i][j] != workingArray[i][j])
                    return false;
            }
        }
        return true;
    }

    private void showLoading(){

    }
}