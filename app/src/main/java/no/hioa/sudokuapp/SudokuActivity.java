package no.hioa.sudokuapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import no.hioa.sudokuapp.dialog.HighscoreDialogFragment;
import no.hioa.sudokuapp.dialog.HintDialog;
import no.hioa.sudokuapp.dialog.NewGameDialog;
import no.hioa.sudokuapp.dialog.SolveDialog;
import no.hioa.sudokuapp.information.HighscoreEntry;
import no.hioa.sudokuapp.information.Sudoku;
import no.hioa.sudokuapp.mysql.HighscoreSQLHelper;
import no.hioa.sudokuapp.mysql.InitDatabase;
import no.hioa.sudokuapp.util.SudokuAdapter;
import no.hioa.sudokuapp.mysql.SudokuSQLHelper;
import no.hioa.sudokuapp.util.SudokuSolveTask;
import no.hioa.sudokuapp.util.SudokuTimer;
import no.hioa.sudokuapp.util.SudokuUtils;

/**
 * Created by Sondre on 03.11.2014.
 *
 * Contains main sudoku-methods and keeps track of game-logic, highscore-access and dialogfragments
 */
public class SudokuActivity extends Activity {

    private int selectedNumber = 0; // selected number in the numbers-layout
    private int selectedSquareX = -1, selectedSquareY = -1; // coordinates of the selected square

    private boolean legal = true; // if an illegal move has been made, used so you can't continue if legal != true
    private boolean hintUsed = false;
    private boolean cheats = false; // if a highscore can be saved even though it was solved with hints/solve

    private int difficulty = 0;
    private int nextDifficulty = 0; // used when user has changed difficulty, but not confirmed a new game

    private static final int NUM_MODE = 0, SQUARE_MODE = 1;
    private static int mode = SQUARE_MODE;

    private Sudoku sudoku;
    private Sudoku solved;
    private SudokuAdapter adapter;

    private SudokuTimer timer;

    private int currentSudoku = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getBooleanExtra("EXIT", false)) finish();

        setContentView(R.layout.activity_sudoku);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        cheats = sharedPref.getBoolean("cheat_preference", false);

        initSudoku();
        initNumbers();
        InitDatabase.initSudokuDB(this);
        InitDatabase.initHighscoreDB(this);

        newGame(currentSudoku);

        /*  if(savedInstanceState == null) {


        }
      else {

            currentSudoku = savedInstanceState.getInt("currentSudoku");
            newGame(currentSudoku);

            sudoku.setSudoku(savedInstanceState.getString("sudoku"));
            //savedInstanceState.getString("solved", solved.getSudokuString());
            //savedInstanceState.getString("legal", sudoku.getLegalString());
            selectedSquareY = savedInstanceState.getInt("y");
            selectedSquareX = savedInstanceState.getInt("x");
            selectedNumber = savedInstanceState.getInt("value");

            timer.setScore(savedInstanceState.getInt("score"));

            legal = savedInstanceState.getBoolean("legal");
            hintUsed = savedInstanceState.getBoolean("hintused");
            mode = savedInstanceState.getInt("mode");

            adapter.updateHighlight(selectedSquareY, selectedSquareX);

            if(mode == NUM_MODE) {
                TextView view = (TextView) findViewById(R.id.button_mode);
                view.setBackground(getResources().getDrawable(R.drawable.num_mode));
            }
            else {
                TextView view = (TextView) findViewById(R.id.button_mode);
                view.setBackground(getResources().getDrawable(R.drawable.square_mode));
            }
        }*/

        Log.d("SudokuAppLog", "Initiated");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("sudoku", sudoku.getSudokuString());
        savedInstanceState.putString("solved", solved.getSudokuString());
        savedInstanceState.putString("legal", sudoku.getLegalString());
        savedInstanceState.putInt("y", selectedSquareY);
        savedInstanceState.putInt("x", selectedSquareX);
        savedInstanceState.putInt("value", selectedNumber);
        savedInstanceState.putInt("currentSudoku", currentSudoku);
        savedInstanceState.putInt("score", timer.getScore());
        savedInstanceState.putBoolean("legal", legal);
        savedInstanceState.putBoolean("hintused", hintUsed);
        savedInstanceState.putInt("mode", mode);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sudoku, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.new_easy_game:
                nextDifficulty = 0;
                newGameDialog();
                return true;
            case R.id.new_normal_game:
                nextDifficulty = 1;
                newGameDialog();
                return true;
            case R.id.new_hard_game:
                nextDifficulty = 2;
                newGameDialog();
                return true;
            case R.id.new_impossible_game:
                nextDifficulty = 3;
                newGameDialog();
                return true;
            case R.id.user_made_game:
                nextDifficulty = 4;
                newGameDialog();
                return true;
            case R.id.solve:
                solveDialog();
                return true;
            case R.id.highscore:
                highscore();
                return true;
            case R.id.exit:
                exitDialog();
                return true;
            case android.R.id.home:
                backDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        backDialog();
        super.onBackPressed();
    }


    public void backDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.back_dialog))
                .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(timer != null) timer.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(timer != null) timer.resume();
    }

    /***** MENU METHODS ****/
    public void nextGame() {
        if(nextDifficulty != difficulty)
            currentSudoku = 0;

        difficulty = nextDifficulty;
        newGame(currentSudoku);
    }

    // initiates a new sudoku
    public void newGame(int id) {
        SudokuSQLHelper db = new SudokuSQLHelper(this);
        List<Sudoku> sudokus = db.getAllSudokus(difficulty);
        int length = sudokus.size();
        if(length == 0) {
            Toast.makeText(this, getResources().getString(R.string.sudoku_notfound_difficulty), Toast.LENGTH_SHORT).show();
            return;
        }
        if(id > length-1) currentSudoku = id = 0;
        currentSudoku++;

        sudoku = sudokus.get(id);
        adapter = new SudokuAdapter(this, sudoku, true);
        adapter.updateSudokuAdapter(sudoku);
        legal = true;
        hintUsed = false;

        if(timer == null)timer = new SudokuTimer((TextView) findViewById(R.id.timerTextView), (TextView) findViewById(R.id.scoreTextView));
        else timer.restart();

        legal = false;
        new SudokuSolveTask(this, sudoku, false, false, false).execute();

        Log.d("SudokuActivity", "New game initiated");
    }

    public void hint() {
        if(legal && SudokuUtils.matches(sudoku, solved)) {
            if (selectedSquareY != -1 && selectedSquareX != -1) {
                hintUsed = true;
                setNumberSquare(solved.get(selectedSquareY, selectedSquareX));
                Log.d("hint()", "showing hint at x: " + selectedSquareX + ", y: " + selectedSquareY + ", " + solved.get(selectedSquareY, selectedSquareX) + "");
            }
            else
                Toast.makeText(this, getResources().getString(R.string.sudoku_selectsquare_complaint), Toast.LENGTH_SHORT).show();
        }
    }

    public void solve() {
        if(!legal) return;
        if(SudokuUtils.matches(sudoku, solved)) {
            sudoku = solved.clone();
            adapter.updateSudokuAdapter(sudoku);
            if (timer != null) timer.complete();
            if(cheats) newHighscore(); // used to test highscores
            Log.d("Solve", "Showing solution!");
        } else {
            Log.d("Solve", "Original sudoku-solve doesn't match current sudoku!");
            new SudokuSolveTask(this, sudoku, true, false, false).execute();
        }
        hintUsed = true;
        legal = false;
    }

    // called by SudokuSolveTask after finishing solving sudoku
    public void saveSolvedSudoku(Sudoku solved, boolean solve, boolean hint) {
        this.solved = solved;

        if(solve) {
            if (solved == null) {
                Toast.makeText(this, getResources().getString(R.string.solution_nonexist), Toast.LENGTH_SHORT).show();
                Log.d("Solve", "Sudoku can't be solved!");
                legal = true;
            } else {
                sudoku = solved.clone();
                adapter.updateSudokuAdapter(sudoku);
                if (timer != null) timer.complete();
            }
            if(cheats) newHighscore(); // used to test highscores
        } else if(hint) {
            newHintDialog();
        }
        else {
            Log.d("Solve", "Sudoku solved, but hidden");
            legal = true;
        }

    }

    public void highscore() {
        Intent intent = new Intent(this, HighscoreActivity.class);
        intent.putExtra("sudokuid", sudoku.getId());
        startActivity(intent);
    }

    public void exit() {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }


    /***** DIALOG METHODS ****/
    public void exitDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        exit();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.exit_dialog))
                .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
    }

    public void newGameDialog() {
        SudokuSQLHelper db = new SudokuSQLHelper(this);
        List<Sudoku> sudokus = db.getAllSudokus(nextDifficulty);
        if(sudokus.size() == 0) {
            Toast.makeText(this, getResources().getString(R.string.sudoku_notfound_difficulty), Toast.LENGTH_SHORT).show();
            return;
        }
        new NewGameDialog().show(getFragmentManager(), "NewGameDialog");
    }

    public void newHintDialog() {
        new HintDialog().show(getFragmentManager(), "HintDialog");
    }

    public void solveDialog() {
        new SolveDialog().show(getFragmentManager(), "SolveDialog");
    }

    public void newHighscoreDialogFragment() {
        new HighscoreDialogFragment().show(getFragmentManager(), "HighscoreDialogFragment");
    }


    /***** HIGHSCORE-RELATED METHODS ***/
    public void addHighscore(String name) {
        Log.d("name.length", name.length()+"");
        if(name.equals("")) {
            Toast.makeText(this, getResources().getString(R.string.name_empty), Toast.LENGTH_SHORT).show();
            newHighscoreDialogFragment();
            return;
        }
        if(name.length() > 10) {
            Toast.makeText(this, getResources().getString(R.string.name_too_long), Toast.LENGTH_SHORT).show();
            newHighscoreDialogFragment();
            return;
        }

        HighscoreSQLHelper db = new HighscoreSQLHelper(this);
        int score = timer.getScore();
        db.addHighscore(new HighscoreEntry(sudoku.getId(), name, score));

        newGameDialog();
    }

    public void newHighscore() {
        HighscoreSQLHelper db = new HighscoreSQLHelper(this);
        List<HighscoreEntry> highscores = db.getAllHighscoresFromSudoku(sudoku.getId());
        sortList(highscores);

        if(highscores.size() < 10) {
            // new highscore
            newHighscoreDialogFragment();
            return;
        }
        int index = getHighscoreIndex(highscores, timer.getScore());
        if (index > -1 && index < 10){
            // new highscore
            newHighscoreDialogFragment();
        }
    }

    private int getHighscoreIndex(List<HighscoreEntry> highscores, int score) {
        for(int i = 0; i < highscores.size(); i++)
            if(highscores.get(i).getScore() < score) return i;

        return -1;
    }

    // sorts a list after highscore-score
    private static void sortList(List<HighscoreEntry> list) {
        for(int i = 0; i < list.size(); i++) {
            for(int j = 0; j < list.size(); j++) {
                if(list.get(i) != list.get(j) && list.get(i).getScore() > list.get(j).getScore()) {
                    HighscoreEntry temp = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, temp);
                }
            }
        }

    }


    /**** GUI-RELATED METHODS ****/

    // sets up sudoku-layout
    public void initSudoku() {
        for(int y = 0; y < 9; y++){
            for(int x = 0; x < 9; x++) {
                TextView view = (TextView) findViewById(getResources().getIdentifier("square_" + x + y, "id", getPackageName()));
                view.setTag(x + "" + y);
                view.setClickable(true);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onSquareClick(v);
                    }
                });
            }
        }
    }

    // sets up numbers-layout
    public void initNumbers() {
        for(int i = 0; i < 10; i++) {
            Button button = (Button) findViewById(getResources().getIdentifier("num_button_" + i, "id", getPackageName()));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClick(v);
                }
            });
        }
        Button button = (Button) findViewById(R.id.num_button_0);
        button.setBackground(getResources().getDrawable(R.drawable.button_selected));
    }

    // when a button in the numbers-layout is clicked
    public void onButtonClick(View v) {
        resetButton();

        Button button = (Button) v;
        selectedNumber = Integer.parseInt(button.getText().toString());
        button.setBackground(getResources().getDrawable(R.drawable.button_selected));

        if(mode == NUM_MODE)
            setNumberSquare(selectedNumber);
    }

    // when a square in the sudoku-layout is clicked
    public void onSquareClick(View v) {
        resetSquare();

        String id = v.getTag().toString();
        selectedSquareX = Integer.parseInt(id.substring(0, 1));
        selectedSquareY = Integer.parseInt(id.substring(1, 2));
        adapter.updateHighlight(selectedSquareY, selectedSquareX);
        TextView textView = (TextView) v;
        textView.setBackground(getResources().getDrawable(R.drawable.square_selected));

        if(mode == SQUARE_MODE)
            setNumberSquare(selectedNumber);
    }

    // switches input-mode
    public void onButtonModeClick(View v) {
        if(mode == NUM_MODE) {
            mode = SQUARE_MODE;
            TextView view = (TextView) findViewById(R.id.button_mode);
            view.setBackground(getResources().getDrawable(R.drawable.square_mode));
        }
        else {
            mode = NUM_MODE;
            TextView view = (TextView) findViewById(R.id.button_mode);
            view.setBackground(getResources().getDrawable(R.drawable.num_mode));
        }
    }

    public void onButtonHintClick(View v) {
        if(legal && SudokuUtils.matches(sudoku, solved)){
            if(selectedSquareY != -1 && selectedSquareX != -1 && sudoku.get(selectedSquareY, selectedSquareX) == 0) {
                newHintDialog();
            }
            else if(sudoku.get(selectedSquareY, selectedSquareX) != 0)
                Toast.makeText(this, getResources().getString(R.string.sudoku_selectemptysquare_complaint), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getResources().getString(R.string.sudoku_selectsquare_complaint), Toast.LENGTH_SHORT).show();
        }
        else
            new SudokuSolveTask(this, sudoku, false, false, true).execute();
    }

    // game-logic, when user has clicked either num or square
    public void setNumberSquare(int value) {
        if((!legal && !adapter.isIllegal()) || selectedSquareY == -1 || selectedSquareX == -1)
            return;

        legal = adapter.setNumberSquare(sudoku, legal, selectedSquareY, selectedSquareX, value);

        if(SudokuUtils.isComplete(sudoku)) {
            timer.complete();
            legal = false; // game is solved

            if(!hintUsed || cheats) { // if hint or solve have not been used, or if it is allowed
                newHighscore();
            }
        }
    }

    public void resetButton() {
        Button button = (Button) findViewById(getResources().getIdentifier("num_button_" + selectedNumber, "id", getPackageName()));
        button.setBackground(getResources().getDrawable(R.drawable.button));
    }

    public void resetSquare() {
        if(selectedSquareX != -1 || selectedSquareY != -1) {
            TextView square = (TextView) findViewById(getResources().getIdentifier("square_" + selectedSquareX + selectedSquareY, "id", getPackageName()));
            square.setBackground(getResources().getDrawable(R.drawable.square));
            adapter.resetHighlight(selectedSquareY, selectedSquareX);
        }
    }
}
