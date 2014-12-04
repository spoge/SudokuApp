package no.hioa.sudokuapp.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import no.hioa.sudokuapp.SudokuCreateActivity;
import no.hioa.sudokuapp.information.Sudoku;
import no.hioa.sudokuapp.SudokuActivity;

/**
 * Created by Sondre on 24.11.2014.
 *
 * An ASyncTask for solving sudoku.
 * Runs as a separate thread
 */

public class SudokuSolveTask extends AsyncTask<Void,Void,Void>
{
    private Context context;
    private Sudoku sudoku;
    private boolean solve;  // if the result is to be shown/hidden
    private boolean create; // if it is in SudokuActivity or SudokuCreateActivity
    private boolean hint;   // if the solution is to be shown as a hint

    public SudokuSolveTask(Context context, Sudoku sudoku, boolean solve, boolean create, boolean hint) {
        this.context = context;
        this.sudoku = sudoku.clone();
        this.solve = solve;
        this.create = create;
        this.hint = hint;
    }

    @Override
    protected Void doInBackground(Void... params) {
        sudoku = SudokuUtils.solveTimed(sudoku);
        if(sudoku == null) Log.d("ASyncTask", "Sudoku can't be solved");
        else if(SudokuUtils.isComplete(sudoku)) Log.d("ASyncTask", "Sudoku solved!");
        else Log.d("ASyncTask", "Something during solving went wrong!");
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if(!create) ((SudokuActivity)context).saveSolvedSudoku(sudoku, solve, hint);
        else ((SudokuCreateActivity)context).saveSolvedSudoku(sudoku, solve);
    }

}