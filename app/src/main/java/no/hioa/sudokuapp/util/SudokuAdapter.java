package no.hioa.sudokuapp.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import no.hioa.sudokuapp.R;
import no.hioa.sudokuapp.information.Square;
import no.hioa.sudokuapp.information.Sudoku;

/**
 * Created by Sondre on 09.11.2014.
 *
 * Contains the views representing the
 * sudoku, and is basicly a link between
 * the sudoku-logic and the sudoku-gui
 */
public class SudokuAdapter {
    private ArrayList<TextView> sudokuAdapter; // array of the TextViews representing the sudoku
    private Context context;

    private Square illegalSquare; // keeps track of which square is the cause of the illegal state
    private ArrayList<Square> illegalSquares; // array of all conflicting squares related to the one illegal square

    private boolean solveMode; // true = solve-mode, false = create-mode

    public SudokuAdapter(Context context, Sudoku sudoku, boolean solveMode){
        this.context = context;
        sudokuAdapter = new ArrayList<TextView>();
        setSudokuAdapter();

        TextView view = (TextView) ((Activity)context).findViewById(R.id.difficultyTextView);
        view.setText(sudoku.getDifficultyString(context));
        this.solveMode = solveMode;
        Log.d("SudokuAdapter","Sudoku loaded, id:" + sudoku.getId()+ ", difficulty: " + sudoku.getDifficultyString(context));
    }

    public TextView getTextViewSquare(int y, int x) {
        return sudokuAdapter.get(x + y * 9);
    }

    public void setSudokuAdapter() {
        for(int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                TextView textView = (TextView) ((Activity)context).findViewById(context.getResources().getIdentifier("square_" + x + y, "id", context.getPackageName()));
                sudokuAdapter.add(textView);
            }
        }
    }

    // updates the entire sudoku-board
    public void updateSudokuAdapter(Sudoku sudoku) {
        for(int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                TextView textView = sudokuAdapter.get(x + y * 9);
                int value = sudoku.get(y, x);
                if(value != 0) textView.setText(value + "");
                else textView.setText("");
                if(sudoku.isLegal(y,x)) textView.setTextColor(context.getResources().getColor(R.color.square_text_dimmed));
                else textView.setTextColor(context.getResources().getColor(R.color.square_text));
            }
        }
    }

    // updates only selected square
    public void updateSquare(Sudoku sudoku, int y, int x, int value) {
        TextView textView = sudokuAdapter.get(x + y * 9);
        if(value != 0) textView.setText(value + "");
        else textView.setText("");
    }

    // removes highlight for given square
    public void resetHighlight(int y, int x) {
        for(int i = 0; i < 9; i++) {
            TextView textView = sudokuAdapter.get(i + y * 9);
            textView.setBackground(context.getResources().getDrawable(R.drawable.square));
        }
        for(int j = 0; j < 9; j++) {
            TextView textView = sudokuAdapter.get(x + j * 9);
            textView.setBackground(context.getResources().getDrawable(R.drawable.square));
        }

        if(illegalSquare != null) {
            TextView textView = sudokuAdapter.get(illegalSquare.getX() + illegalSquare.getY() * 9);
            textView.setBackground(context.getResources().getDrawable(R.drawable.square_selected));
        }
    }

    // adds highlight for given square
    public void updateHighlight(int y, int x) {
        for(int i = 0; i < 9; i++) {
            TextView textView = sudokuAdapter.get(i + y * 9);
            textView.setBackground(context.getResources().getDrawable(R.drawable.square_highlighted));
            if(illegalSquare != null && illegalSquare.getX() == i && illegalSquare.getY() == y)
                textView.setBackground(context.getResources().getDrawable(R.drawable.square_selected_highlighted));
        }
        for(int j = 0; j < 9; j++) {
            TextView textView = sudokuAdapter.get(x + j * 9);
            textView.setBackground(context.getResources().getDrawable(R.drawable.square_highlighted));
            if(illegalSquare != null && illegalSquare.getX() == x && illegalSquare.getY() == j)
                textView.setBackground(context.getResources().getDrawable(R.drawable.square_selected_highlighted));
        }
    }

    // when a number is being set on the board, and handles any illegal states if-any
    public boolean setNumberSquare(Sudoku sudoku, boolean isLegal, int y, int x, int value) {
        boolean legal = isLegal;

        if(!isLegal && illegalSquare != null && illegalSquare.getX() == x && illegalSquare.getY() == y) {
            sudoku.set(y, x, value);
            updateSquare(sudoku, y, x, value);
            Log.d("Sudoku", "Put illegal " + value + " at x: " + x + ", y: " + y);

            for(Square s : illegalSquares){
                TextView err = getTextViewSquare(s.getY(), s.getX());
                if(sudoku.isLegal(s.getY(), s.getX())) {
                    if(solveMode) err.setTextColor(context.getResources().getColor(R.color.square_text_dimmed));
                    else err.setTextColor(context.getResources().getColor(R.color.square_text));
                }
                else err.setTextColor(context.getResources().getColor(R.color.square_text));
            }
        }
        else if (!isLegal) return false;

        TextView square = getTextViewSquare (y, x);

        // LOGIC
        if(sudoku.isLegal(y, x)) {
            if(value == 0 || isLegal) {
                sudoku.set(y, x, value);
                updateSquare(sudoku, y, x, value);
                Log.d("Sudoku", "Put " + value + " at x: " + x + ", y: " + y);
            }

            if(solveMode) square.setTextColor(context.getResources().getColor(R.color.square_text_dimmed));
            else square.setTextColor(context.getResources().getColor(R.color.square_text));

            // contains all squares that conflicts with the selected square
            illegalSquares = SudokuUtils.getIllegalSquares(sudoku, y, x, value);
            if(illegalSquares.size() > 0) {
                illegalSquare = new Square(y, x);
                square.setTextColor(context.getResources().getColor(R.color.square_text_error));


                for(Square s : illegalSquares) {
                    TextView err = getTextViewSquare(s.getY(), s.getX());
                    err.setTextColor(context.getResources().getColor(R.color.square_text_error));
                    legal = false;
                }
            }
            else {
                illegalSquare = null;
                legal = true;
            }
            Log.d("illegal", illegalSquares.toString());
        }
        return legal;
    }

    public void setTextColor(int color) {
        for(int x = 0; x < 9; x++) {
            for(int y = 0; y < 9; y++) {
                TextView v = getTextViewSquare(y, x);
                v.setTextColor(color);
            }
        }
    }

    public boolean isIllegal() {
        if(illegalSquare == null) return false;
        return true;
    }
}
