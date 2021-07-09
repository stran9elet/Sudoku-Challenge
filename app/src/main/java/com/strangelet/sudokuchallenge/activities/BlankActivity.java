package com.strangelet.sudokuchallenge.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.strangelet.sudokuchallenge.R;
import com.strangelet.sudokuchallenge.utils.SudokuMaker;

public class BlankActivity extends AppCompatActivity {
    Handler handler;
    Runnable runnable;
    private String target;
    private StartGameActivityTask startGameActivityTask;
    private static final int BACK_TO_MAIN_ACTIVITY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        startGameActivityTask = null;

        Intent intent = getIntent();
        target = intent.getStringExtra("target");

        switch (target) {
            case "GameActivity":
                if(getIntent().getIntExtra("difficulty", 0) == SudokuMaker.DIABOLICAL
                || getIntent().getIntExtra("difficulty", 0) == SudokuMaker.TRICKY
                || getIntent().getIntExtra("difficulty", 0) == SudokuMaker.FIENDISH) {
                    TextView extraTimeView = findViewById(R.id.extra_time_view);
                    extraTimeView.setVisibility(View.VISIBLE);
                }
                startGameActivityTask = new StartGameActivityTask(this);
                startGameActivityTask.execute(getIntent());
                break;

            case "MainActivity":
                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent1 = new Intent(BlankActivity.this, MainActivity.class);
                        intent1.putExtra("noBoardDrop", true);
                        startActivity(intent1);
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                };
                handler.postDelayed(runnable, 1050); //This much time your blank activity will remain open
                break;
            default:
                handler.removeCallbacks(runnable);
                finish();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(handler!=null && runnable!=null)
            handler.removeCallbacks(runnable);
        if (startGameActivityTask != null)
            startGameActivityTask.cancel(true);
        Intent intent1 = new Intent(BlankActivity.this, MainActivity.class);
        intent1.putExtra("noBoardDrop", true);
        startActivity(intent1);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    };



}
class StartGameActivityTask extends AsyncTask<Intent, Void, SudokuMaker>{
    private Context context;
    String gameType;
    int difficulty;
    boolean isDailyChallenge;
    public StartGameActivityTask(Context context){
        this.context = context;
    }

    @Override
    protected SudokuMaker doInBackground(Intent... intents) {
        gameType = intents[0].getStringExtra("gameType");
        difficulty = intents[0].getIntExtra("difficulty", SudokuMaker.MEDIUM);
        isDailyChallenge = intents[0].getBooleanExtra("dailyChallenge", false);
        SudokuMaker sudokuMaker = new SudokuMaker(difficulty);
        if(isCancelled())
            return null;
        return sudokuMaker;
    }

    @Override
    protected void onPostExecute(SudokuMaker sudokuMaker) {
        if (sudokuMaker != null) {
            Intent intent1 = new Intent(context, GameActivity.class);
            intent1.putExtra("gameType", gameType);
            intent1.putExtra("difficulty", difficulty);
            intent1.putExtra("dailyChallenge", isDailyChallenge);
            intent1.putExtra("sudokuMaker", sudokuMaker);
            context.startActivity(intent1);
            ((Activity) context).finish();
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}

