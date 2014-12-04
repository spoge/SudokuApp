package no.hioa.sudokuapp.util;

import android.util.Log;
import android.widget.TextView;

import android.os.Handler;
import android.widget.TextView;

/**
 * Created by Sondre on 20.11.2014.
 *
 * Handles timer and score during the game
 */
public class SudokuTimer {

    private TextView timerTextView;
    private TextView scoreTextView;

    private int time = 0;
    private int score = 10000;

    private Handler timerHandler;
    private Runnable timerRunnable;

    private boolean complete = false;   // if sudoku is complete
    private boolean running = false;    // if timer is paused

    public SudokuTimer(TextView timerView, TextView scoreView) {
        timerTextView = timerView;
        scoreTextView = scoreView;

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if(time < 10000 || time == 0) score = 10000 - time;
                else score = 0;

                int seconds = time % 60;
                int minutes = time / 60;

                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
                scoreTextView.setText("Score: " + score);
                timerHandler.postDelayed(this, 1000);
                time++;
            }
        };

        timerHandler.postDelayed(timerRunnable, 0);
        running = true;
        Log.d("Timer", "Timer started");
    }

    public void restart() {
        time = 0;
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 0);
        Log.d("Timer", "Timer restarted");
    }

    public void stop() {
        timerHandler.removeCallbacks(timerRunnable);
        running = false;
        Log.d("Timer", "Timer stopped");
    }

    public void resume() {
        if(complete) return;
        if(!running) timerHandler.postDelayed(timerRunnable, 0);
        running = true;
        Log.d("Timer", "Timer resumed");
    }

    public void complete() {
        timerHandler.removeCallbacks(timerRunnable);
        running = false;
        complete = true;
        Log.d("Timer", "Timer finished");
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        time = 10000 - score;
    }

    public boolean isRunning() {
        return running;
    }
}
