package no.hioa.sudokuapp.util;

import android.util.Log;

import java.util.ArrayList;

import no.hioa.sudokuapp.information.Square;
import no.hioa.sudokuapp.information.Sudoku;

/**
 * Created by Sondre on 09.11.2014.
 *
 * Calculates legal moves and contains solving methods
 */
public class SudokuUtils {

    public static Sudoku solve(Sudoku sudokuIn) {
        Sudoku sudoku = sudokuIn.clone();
        try {
            sudoku.countFreeSpaces();
            if(solveInner(sudoku)) return sudoku;
        }
        catch (Exception e) {
            errorMessage();
        }
        return null;
    }

    public static Sudoku solveTimed(Sudoku sudokuIn) {
        Sudoku sudoku;
        long start = System.currentTimeMillis();
        sudoku = solve(sudokuIn);
        long time = System.currentTimeMillis() - start;
        Log.d("SudokuAppLog", "solved in " + time + " msecs");
        return sudoku;
    }

    // returns false if the board is in an unsolvable state, returns true if solution to whole board is found
    private static boolean solveInner(Sudoku sudoku) throws Exception {
        ArrayList<Integer> possibleValues; 					// array of possibilities for specified square
        int lowest = updatePossible(sudoku);				// lowest possibilities of all square
        int counter = sudoku.getCounter(); 					// counts free/empty spaces
        for(int y = 0; y < 9; y++){
            for(int x = 0; x < 9; x++){

                int current = sudoku.get(y, x);				// current value
                if (current != 0 && counter == 0) return true; // already solved! (square with no possibilities, and every square on board is taken)
                if (current != 0) continue;					// square already taken
                possibleValues = getPossible(y, x, sudoku);	// possible values for current square
                int size = possibleValues.size();
                if (current == 0 && size == 0 && counter > 0) return false; // untaken square, with no possibilities
                if (current == 0 && (size == 0 || size != lowest)) continue;
                if (current == 0 && size > 0 && size == lowest) if(solveRecursive(y, x, sudoku, possibleValues)) return true; // solves square with lowest num of possiblilities board solved
                return false; // no solution, backtrack
            }
        }
        return false; // should never be reached
    }

    // iterates through the valid possible values for a specific square, returns false if board is in an unsolvable state, returns true if board is solved
    private static boolean solveRecursive(int y, int x, Sudoku sudoku, ArrayList<Integer> possibleValues) throws Exception {
        sudoku.decrementCounter();
        for(Integer i : possibleValues){
            sudoku.set(y, x, i);
            if(solveInner(sudoku)) return true;
        }
        sudoku.incrementCounter();
        sudoku.set(y, x, 0);
        return false;
    }

    // updates the possibility-array, and returns a call from lowestPossibility()
    private static int updatePossible(Sudoku sudoku) throws Exception {
        int[][] possible = new int[9][9];
        for (int y = 0; y < 9; y++) for (int x = 0; x < 9; x++)	if (sudoku.get(y, x) == 0) possible[y][x] = getPossible(y, x, sudoku).size();
        return lowestPossibility(sudoku, possible);
    }

    // returns lowest number of values for a possible solution for empty square on the board
    private static int lowestPossibility(Sudoku sudoku, int[][] possible) {
        int n = 10;
        for (int y = 0; y < 9; y++)	for (int x = 0; x < 9; x++) if (sudoku.get(y, x) == 0 && possible[y][x] != 0 && possible[y][x] < n) n = possible[y][x];
        return n;
    }

    // returns whether or not the board is in a legal state
    private static boolean legal(Sudoku sudoku) {
        for(int y = 0; y < 9; y++){
            for(int x = 0; x < 9; x++){
                int temp = sudoku.get(y, x);
                sudoku.set(y, x, 0);
                try {
                    if(getPossible(y, x, sudoku).size() == 0) return false; }
                catch (Exception e) { errorMessage(); return false;	}
                sudoku.set(y, x, temp);
            }
        }
        return true;
    }

    public static boolean isLegal(Sudoku sudokuIn) {
        Sudoku sudoku = sudokuIn.clone();
        return legal(sudoku);
    }

    // returns whether or not the board is in a completed legal state
    private static boolean complete(Sudoku sudoku) {
        try { if(!legal(sudoku)) return false; }
        catch (Exception e) { errorMessage(); return false;	}
        for(int y = 0; y < 9; y++) for(int x = 0; x < 9; x++) if(sudoku.get(y, x) == 0) return false;
        return true;
    }

    public static boolean isComplete(Sudoku sudokuIn) {
        Sudoku sudoku = sudokuIn.clone();
        return complete(sudoku);
    }

    public static ArrayList<Square> getIllegalSquares(Sudoku sudoku, int y, int x, int value) {
        ArrayList<Square> illegal = new ArrayList<Square>();
        if(sudoku == null || value == 0) return illegal;

        for(int i = 0; i < 9; i++) if (x != i && sudoku.get(y, i) == value && !contains(illegal, y, i)) illegal.add(new Square(y, i, value));
        for(int i = 0; i < 9; i++) if (y != i && sudoku.get(i, x) == value && !contains(illegal, i, x)) illegal.add(new Square(i, x, value));

        int yy = y / 3 * 3; // first y square in sector
        int xx = x / 3 * 3; // first x square in sector
        for (int i = yy; i < yy + 3; i++) for (int j = xx; j < xx + 3; j++)	if (y != i && x != j && sudoku.get(i, j) == value && !contains(illegal, i, j)) illegal.add(new Square(i, j, value));

        return illegal;
    }

    private static boolean contains(ArrayList<Square> squares, int y, int x) {
        if(squares.size() < 1) return false;

        for(Square square : squares)
            if(square.equals(y, x)) return true;
        return false;
    }

    // gets possible values for y, x
    private static ArrayList<Integer> getPossible(int y, int x, Sudoku sudoku) throws Exception {
        ArrayList<Integer> possible = new ArrayList<Integer>();
        if(sudoku.get(y, x) != 0) return possible;

        boolean[] p = new boolean[10];
        for(int i = 0; i < p.length; i++) p[i] = true;

        for(int i = 0; i < 9; i++) if (sudoku.get(y, i) != 0) p[sudoku.get(y, i)] = false;
        for(int i = 0; i < 9; i++) if (sudoku.get(i, x) != 0) p[sudoku.get(i, x)] = false;

        int yy = y / 3 * 3; // first y square in sector
        int xx = x / 3 * 3; // first x square in sector
        for (int i = yy; i < yy + 3; i++) for (int j = xx; j < xx + 3; j++)	if (sudoku.get(i, j) != 0) p[sudoku.get(i, j)] = false;

        for(int i = 1; i < 10; i++) if(p[i]) possible.add(i);
        return possible;
    }

    // returns true if the solved is a solved variant of original
    public static boolean matches(Sudoku original, Sudoku solved) {
        if(original == null || solved == null) return false;
        for(int y = 0; y < 9; y++) {
            for(int x = 0; x < 9; x++) {
                if(original.get(y, x) != 0 && original.get(y, x) != solved.get(y, x)) return false;
            }
        }
        return true;
    }

    // returns if numbers to be valid sudoku
    public static boolean isEnoughNumbers(Sudoku sudoku) {
        int count = 0;
        for(int y = 0; y < 9; y++) {
            for(int x = 0; x < 9; x++) {
                if(sudoku.get(y,x) != 0) count++;
            }
        }
        if(count < 15) return false;
        return true;
    }

    private static void errorMessage() {
        Log.d("SudokuAppLog", "INCONSISTENT SUDOKU!");
    }
}
