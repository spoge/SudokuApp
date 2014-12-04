package no.hioa.sudokuapp.information;

import android.content.Context;
import android.util.Log;

import no.hioa.sudokuapp.R;

/**
 * Created by Sondre on 09.11.2014.
 *
 * Contains the sudoku-puzzle,
 * and simple relevant methods
 */
public class Sudoku {

    private int id;
    private int difficulty = 0; // 0 = easy, 1 = normal, 2 = hard, 3 = impossible, 4 = user-made
    private int[][] sudoku;     // the sudoku itself
    private boolean [][] legal; // keeps track of changeable squares, to not mess up the original sudoku

    private int counter = 9 * 9; // number of empty squares in sudoku

    public Sudoku() {
        sudoku = new int[9][9];
        legal = new boolean[9][9];
        for(int y = 0; y < 9; y++) for(int x = 0; x < 9; x++) legal[y][x] = true;
    }

    public Sudoku(int[][] sudoku) {
        this.sudoku = sudoku;
        legal = setLegalArray();
    }

    public Sudoku(int[][] sudoku, boolean[][] legal) {
        this.sudoku = sudoku;
        this.legal = legal;
    }

    public Sudoku(int id, int difficulty, int[][] sudoku, boolean[][] legal) {
        this.id = id;
        this.difficulty = difficulty;
        this.sudoku = sudoku;
        this.legal = legal;
    }

    public Sudoku(String sudokuIn) {
        sudoku = parseSudoku(sudokuIn);
        legal = setLegalArray();
    }

    public Sudoku(int difficulty, String sudokuIn) {
        this.difficulty = difficulty;
        sudoku = parseSudoku(sudokuIn);
        legal = setLegalArray();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setSudoku(String sudoku) {
        this.sudoku = parseSudoku(sudoku);
        legal = setLegalArray();
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public boolean[][] setLegalArray() {
        boolean[][] legal = new boolean[9][9];
        for(int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                if (sudoku[y][x] != 0) legal[y][x] = false;
                else legal[y][x] = true;
            }
        }
        return legal;
    }

    public int[][] parseSudoku(String s) {
        if(s.length() != 9*9) return null;

        int[][] sudoku = new int[9][9];

        char[] c = s.toCharArray();
        int i = 0;

        for(int y = 0; y < 9; y++)
            for(int x = 0; x < 9; x++)
                    sudoku[x][y] = getInt(c[i++]);

        return sudoku;
    }

    private String parseSudoku(int[][] s) {
        String sudoku = "";

        for(int y = 0; y < 9; y++)
            for(int x = 0; x < 9; x++)
                sudoku += s[x][y];

        return sudoku;
    }

    public boolean isLegal(int y, int x) {
        return legal[y][x];
    }

    // resets sudoku to the original puzzle
    public void resetSudoku() {
        for(int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                if(legal[y][x]) sudoku[y][x] = 0;
    }

    public Sudoku clone(){
        int[][] array = new int[9][9];
        for(int y = 0; y < 9; y++) for(int x = 0; x < 9; x++) array[y][x] = sudoku[y][x];

        boolean[][] legalarray = new boolean[9][9];
        for(int y = 0; y < 9; y++) for(int x = 0; x < 9; x++) legalarray[y][x] = legal[y][x];

        return new Sudoku(id, difficulty, array, legalarray);
    }

    private int getInt(char c) {
        int value = Character.getNumericValue(c);
        if(value != -1) return value;
        return 0;
    }

    // sets value of square(y, x)
    public void set(int y, int x, int value) {
        sudoku[y][x] = value;
    }

    // gets value of square(y, x)
    public int get(int y, int x) {
        return sudoku[y][x];
    }

    // returns the number of empty cells on the board, used by SudokuUtils
    public int countFreeSpaces() {
        counter = 0;
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                if (sudoku[y][x] == 0)
                    counter++;
        return counter;
    }

    public void decrementCounter(){
        counter--;
    }

    public void incrementCounter(){
        counter++;
    }

    public int getCounter(){
        return counter;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public String getDifficultyString(Context context) {
        switch (difficulty) {
            case 0: return context.getString(R.string.difficulty_easy);
            case 1: return context.getString(R.string.difficulty_normal);
            case 2: return context.getString(R.string.difficulty_hard);
            case 3: return  context.getString(R.string.difficulty_impossible);
            default: return context.getString(R.string.user_made);
        }
    }

    // for debug
    public void print() {
        StringBuilder s = new StringBuilder();
        s.append('\n').append(' '); for(int i = 0; i < 29; i++) s.append('-'); s.append('\n'); // looong line
        for(int y = 0; y < 9; y++){
            if(y == 3 || y == 6) { s.append(' '); for(int i = 0; i < 29; i++) s.append('-'); s.append('\n'); }
            for(int x = 0; x < 9; x++){
                if(x == 0 || x == 3 || x == 6) s.append('|');
                if(sudoku[x][y] != 0) s.append(' ').append(sudoku[x][y]).append(' ');
                else s.append(' ').append('.').append(' ');
            }
            s.append('|').append('\n');
        }
        s.append(' '); for(int i = 0; i < 29; i++) s.append('-'); s.append('\n');
        Log.d("SUDOKU-SOLVE", s.toString());
    }

    // returns the sudoku as a string
    public String getSudokuString() {
        return parseSudoku(sudoku);
    }

    // returns the original sudoku as a string
    public String getLegalString() {
        String string = "";
        for(int y = 0; y < 9; y++){
            for(int x = 0; x < 9; x++) {
                if(legal[x][y])
                    string += sudoku[x][y];
                else
                    string += 0+"";
            }
        }

        return string;
    }
}
