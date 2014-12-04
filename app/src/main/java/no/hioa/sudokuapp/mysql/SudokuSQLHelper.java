package no.hioa.sudokuapp.mysql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import no.hioa.sudokuapp.information.Sudoku;

/**
 * Created by Sondre on 24.11.2014.
 *
 * Database-class for the sudokus.
 */
public class SudokuSQLHelper extends SQLiteOpenHelper {

    private final static String TABLE_SUDOKUS = "Sudokus";
    private final static String DB_NAME = "SudokuDatabase";

    public final static String KEY_ID = "_ID", KEY_DIFFICULTY = "_DIFFICULTY", KEY_SUDOKU = "_SUDOKU";

    private final static int DB_VERSION = 1;

    public SudokuSQLHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_SUDOKUS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_DIFFICULTY + " INTEGER, " +
                KEY_SUDOKU + " TEXT)";
        sqLiteDatabase.execSQL(createTable);
        Log.d("SQLite", "Database created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SUDOKUS);
        onCreate(sqLiteDatabase);
    }

    // adds a sudoku to the database
    public void addSudoku(Sudoku sudoku){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DIFFICULTY, sudoku.getDifficulty());
        values.put(KEY_SUDOKU, sudoku.getSudokuString());

        db.insert(TABLE_SUDOKUS, null, values);
        db.close();
        Log.d("SQLite", "Sudoku added in database");
    }

    // returns a single sudoku with given ID
    public Sudoku getSudoku(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUDOKUS, new String[]{KEY_ID, KEY_DIFFICULTY, KEY_SUDOKU}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        Sudoku foundSudoku = new Sudoku();
        if(cursor != null) {
            cursor.moveToFirst();

            foundSudoku.setId(Integer.parseInt(cursor.getString(0)));
            foundSudoku.setDifficulty(Integer.parseInt(cursor.getString(1)));
            foundSudoku.setSudoku(cursor.getString(2));
        }
        return foundSudoku;
    }

    // returns all sudokus currently in the database
    public List<Sudoku> getAllSudokus(){
        List<Sudoku> sudokus = new ArrayList<Sudoku>();
        String query = "SELECT * FROM " + TABLE_SUDOKUS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst())
            do{
                Sudoku sudoku = new Sudoku();

                sudoku.setId(Integer.parseInt(cursor.getString(0)));
                sudoku.setDifficulty(Integer.parseInt(cursor.getString(1)));
                sudoku.setSudoku(cursor.getString(2));

                sudokus.add(sudoku);
            }while(cursor.moveToNext());

        return sudokus;
    }

    // returns all sudokus currently in the database with given difficulty
    public List<Sudoku> getAllSudokus(int difficulty){
        List<Sudoku> sudokus = new ArrayList<Sudoku>();
        String query = "SELECT * FROM " + TABLE_SUDOKUS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst())
            do{
                if(Integer.parseInt(cursor.getString(1)) != difficulty) continue;

                Sudoku sudoku = new Sudoku();
                sudoku.setId(Integer.parseInt(cursor.getString(0)));
                sudoku.setDifficulty(Integer.parseInt(cursor.getString(1)));
                sudoku.setSudoku(cursor.getString(2));

                sudokus.add(sudoku);
            }while(cursor.moveToNext());

        return sudokus;
    }

    public void deleteSudoku(Sudoku sudoku){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_SUDOKUS, KEY_ID + " =?", new String[]{String.valueOf(sudoku.getId())});
        db.close();
    }

    public int updateSudoku(Sudoku sudoku){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DIFFICULTY, sudoku.getDifficulty());
        values.put(KEY_SUDOKU, sudoku.getSudokuString());

        return db.update(TABLE_SUDOKUS, values, KEY_ID + "=?", new String[]{String.valueOf(sudoku.getId())});
    }
}