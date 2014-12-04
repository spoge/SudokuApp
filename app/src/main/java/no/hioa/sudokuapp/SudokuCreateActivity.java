package no.hioa.sudokuapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import no.hioa.sudokuapp.information.Sudoku;
import no.hioa.sudokuapp.mysql.SudokuSQLHelper;
import no.hioa.sudokuapp.util.SudokuAdapter;
import no.hioa.sudokuapp.util.SudokuSolveTask;
import no.hioa.sudokuapp.util.SudokuUtils;

/**
 * Created by Sondre on 03.11.2014.
 *
 * Contains sudoku-methods for the create-activity and keeps track of game-logics
 */
public class SudokuCreateActivity extends Activity {

    private int selectedNumber = 0; // selected number in the numbers-layout
    private int selectedSquareX = -1, selectedSquareY = -1; // coordinates of the selected square

    private static final int NUM_MODE = 0, SQUARE_MODE = 1;
    private static int mode = SQUARE_MODE;

    private boolean legal = true; // if an illegal move has been made, used so you can't continue if legal != true
    private boolean toggle = false; // toggle, if solution or sudoku is shown

    private Sudoku sudoku;
    private Sudoku solved;
    private SudokuAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_create);
        setTitle(getResources().getString(R.string.create_sudoku));

        initSudoku();
        initNumbers();

        sudoku = new Sudoku();
        sudoku.setDifficulty(4);

        adapter = new SudokuAdapter(this, sudoku, false);
        adapter.updateSudokuAdapter(sudoku);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sudoku_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.toggle_solve:
                toggleSolve();
                return true;
            case R.id.save_sudoku:
                saveSudoku();
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

    public void exit() {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    // toggles if the solution is shown/hidden
    private void toggleSolve() {
        if(!legal && !toggle) {
            Toast.makeText(this, getResources().getString(R.string.sudoku_not_legal_complain), Toast.LENGTH_SHORT).show();
            return;
        }
        if(SudokuUtils.isComplete(sudoku) && !toggle) {
            Toast.makeText(this, getResources().getString(R.string.sudoku_already_solved_complain), Toast.LENGTH_SHORT).show();
            return;
        }
        toggle = !toggle;
        if(toggle) {
            if(solved == null || !SudokuUtils.matches(sudoku, solved)) solve();
            else {
                adapter.updateSudokuAdapter(solved);
                legal = false;
            }
        }
        else {
            adapter.updateSudokuAdapter(sudoku);
            adapter.setTextColor(getResources().getColor(R.color.square_text));
            legal = true;
        }
    }

    public void saveSudoku() {
        if(SudokuUtils.isComplete(sudoku)) {
            Toast.makeText(this, getResources().getString(R.string.sudoku_complete_complain), Toast.LENGTH_SHORT).show();
            return;
        }
        if(!SudokuUtils.isEnoughNumbers(sudoku)){
            Toast.makeText(this, getResources().getString(R.string.sudoku_not_enough_complain), Toast.LENGTH_SHORT).show();
            return;
        }
        if(!legal && !toggle) {
            Toast.makeText(this, getResources().getString(R.string.sudoku_not_legal_complain), Toast.LENGTH_SHORT).show();
            return;
        }

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        saveToDB();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.save_sudoku_dialog))
                .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
    }

    public void saveToDB() {
        SudokuSQLHelper db = new SudokuSQLHelper(this);
        Log.d("saveToDB", sudoku.getSudokuString());
        db.addSudoku(new Sudoku(4, sudoku.getSudokuString()));
    }

    public void solveSudoku() {
        if(!legal && !toggle) {
            Toast.makeText(this, getResources().getString(R.string.sudoku_not_legal_complain), Toast.LENGTH_SHORT).show();
            return;
        }
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        solve();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.solve_dialog))
                .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
    }

    public void solve() {
        if(!legal) return;

        Sudoku s = new Sudoku(sudoku.getSudokuString());
        new SudokuSolveTask(this, s, true, true, false).execute();
        legal = false;
    }

    // called by SudokuSolveTask after finishing solving sudoku
    public void saveSolvedSudoku(Sudoku solved, boolean solve) {
        this.solved = solved;

        if(solve) {
            if (solved == null) {
                Toast.makeText(this, getResources().getString(R.string.solution_nonexist), Toast.LENGTH_SHORT).show();
                Log.d("Solve", "Sudoku can't be solved!");
                legal = true;
            } else {
                //sudoku = solved.clone();
                adapter.updateSudokuAdapter(solved);
                toggle = true;
                legal = false;
            }
        } else {
            Log.d("Solve", "Sudoku solved, but hidden");
            legal = true;
        }
    }

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

    public void onButtonSolveClick(View v) {
        if(!legal && !toggle) {
            Toast.makeText(this, getResources().getString(R.string.sudoku_not_legal_complain), Toast.LENGTH_SHORT).show();
            return;
        }
        if(!SudokuUtils.isComplete(sudoku) && legal) {
            solveSudoku();
        } else {
            Toast.makeText(this, getResources().getString(R.string.sudoku_already_solved_complain), Toast.LENGTH_SHORT).show();
        }
    }

    // game-logic, when user has clicked either num or square
    public void setNumberSquare(int value) {
        if((!legal && !adapter.isIllegal()) || selectedSquareY == -1 || selectedSquareX == -1)
            return;

        legal = adapter.setNumberSquare(sudoku, legal, selectedSquareY, selectedSquareX, value);

        if(SudokuUtils.isComplete(sudoku)) {

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
