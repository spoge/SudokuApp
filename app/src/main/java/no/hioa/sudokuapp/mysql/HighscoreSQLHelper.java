package no.hioa.sudokuapp.mysql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import no.hioa.sudokuapp.information.HighscoreEntry;
import no.hioa.sudokuapp.information.Sudoku;

/**
 * Created by Sondre on 24.11.2014.
 *
 * Database-class for the highscores.
 */
public class HighscoreSQLHelper extends SQLiteOpenHelper {

    private final static String TABLE_HIGHSCORES = "Highscores";
    private final static String DB_NAME = "HighscoreDatabase";

    public final static String KEY_ID = "_ID", KEY_SUDOKUID = "_SUDOKUID", KEY_NAME = "_NAME", KEY_SCORE = "_SCORE";

    private final static int DB_VERSION = 1;

    public HighscoreSQLHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_HIGHSCORES + "(" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_SUDOKUID + " INTEGER, " +
                KEY_NAME + " TEXT, " +
                KEY_SCORE + " INTEGER)";
        sqLiteDatabase.execSQL(createTable);
        Log.d("SQLite", "Database created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_HIGHSCORES);
        onCreate(sqLiteDatabase);
    }

    // adds a highscore-entry to the database
    public void addHighscore(HighscoreEntry entry){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SUDOKUID, entry.getSudokuId());
        values.put(KEY_NAME, entry.getName());
        values.put(KEY_SCORE, entry.getScore());

        db.insert(TABLE_HIGHSCORES, null, values);
        db.close();
        Log.d("SQLite", "Highscore added in database");
    }

    // returns a single highscore with given ID
    public HighscoreEntry getHighscoreEntry(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HIGHSCORES, new String[]{KEY_ID, KEY_SUDOKUID, KEY_NAME, KEY_SCORE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        HighscoreEntry foundEntry = new HighscoreEntry();
        if(cursor != null) {
            cursor.moveToFirst();

            foundEntry.setId(id);
            foundEntry.setSudokuId(Integer.parseInt(cursor.getString(1)));
            foundEntry.setName(cursor.getString(2));
            foundEntry.setScore(Integer.parseInt(cursor.getString(3)));
        }
        return foundEntry;
    }

    // returns all highscores currently in the database
    public List<HighscoreEntry> getAllHighscores(){
        List<HighscoreEntry> highscore = new ArrayList<HighscoreEntry>();
        String query = "SELECT * FROM " + TABLE_HIGHSCORES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst())
            do{
                HighscoreEntry entry = new HighscoreEntry();

                entry.setId(Integer.parseInt(cursor.getString(0)));
                entry.setSudokuId(Integer.parseInt(cursor.getString(1)));
                entry.setName(cursor.getString(2));
                entry.setScore(Integer.parseInt(cursor.getString(3)));

                highscore.add(entry);
            }while(cursor.moveToNext());

        return highscore;
    }

    // returns all highscores in sudoku with given id currently in the database
    public List<HighscoreEntry> getAllHighscoresFromSudoku(int id){
        List<HighscoreEntry> highscore = new ArrayList<HighscoreEntry>();
        String query = "SELECT * FROM " + TABLE_HIGHSCORES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst())
            do{
                if(Integer.parseInt(cursor.getString(1)) != id) continue;

                HighscoreEntry entry = new HighscoreEntry();
                entry.setId(Integer.parseInt(cursor.getString(0)));
                entry.setSudokuId(Integer.parseInt(cursor.getString(1)));
                entry.setName(cursor.getString(2));
                entry.setScore(Integer.parseInt(cursor.getString(3)));

                highscore.add(entry);
            }while(cursor.moveToNext());

        return highscore;
    }


    public void deleteHighscoreEntry(HighscoreEntry entry){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_HIGHSCORES, KEY_ID + " =?", new String[]{String.valueOf(entry.getId())});
        db.close();
    }

    public int updateHighscore(HighscoreEntry entry){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SUDOKUID, entry.getSudokuId());
        values.put(KEY_NAME, entry.getName());
        values.put(KEY_SCORE, entry.getScore());

        return db.update(TABLE_HIGHSCORES, values, KEY_ID + "=?", new String[]{String.valueOf(entry.getId())});
    }
}