package no.hioa.sudokuapp.mysql;

import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.Random;

import no.hioa.sudokuapp.R;
import no.hioa.sudokuapp.information.HighscoreEntry;
import no.hioa.sudokuapp.information.Sudoku;

/**
 * Created by Sondre on 26.11.2014.
 *
 * Initiates the database, and populates it with some data
 */
public class InitDatabase {

    public static void initSudokuDB(Context context) {
        Log.d("SudokuActivity", "initSudokuDB()");
        SudokuSQLHelper db = new SudokuSQLHelper(context);

        if(db.getAllSudokus().size() > 1) return; // create db if db is not populated

        //deletes db
        List<Sudoku> slist = db.getAllSudokus();
        int length = slist.size();
        for(int i = 0; i < length; i++) db.deleteSudoku(slist.get(i));
        Log.d("SudokuDB", "Length: " + db.getAllSudokus().size());

        String[] sudokus = context.getResources().getStringArray(R.array.sudokus);

        for(int i = 0; i < sudokus.length; i++)
        {
            String[] s = sudokus[i].split(", ");
            db.addSudoku(new Sudoku(Integer.parseInt(s[0]), s[1]));
        }
    }

    public static void initHighscoreDB(Context context) {
        Log.d("SudokuActivity", "initHighscoreDB()");
        SudokuSQLHelper sdb = new SudokuSQLHelper(context);
        HighscoreSQLHelper hdb = new HighscoreSQLHelper(context);

        if(hdb.getAllHighscores().size() > 1) return; // create db if db is not populated

        //deletes db
        List<HighscoreEntry> highscores = hdb.getAllHighscores();
        int length = highscores.size();
        for(int i = 0; i < length; i++) hdb.deleteHighscoreEntry(highscores.get(i));
        highscores.clear();

        Log.d("HighscoreDB", "Length: " + hdb.getAllHighscores().size());

        List<Sudoku> slist = sdb.getAllSudokus();
        Log.d("HighscoreDB", "Length: " + slist.size());

        Random r = new Random();
        for(Sudoku s : slist) {
            if(s.getDifficulty() == 4) continue; // don't auto-populate user-made sudokus
            Log.d("HighscoreDB", "Sudoku " + s.getId());
            highscores.add(new HighscoreEntry(s.getId(), "Sondre", r.nextInt(8000 + 100)));
            highscores.add(new HighscoreEntry(s.getId(), "Per", r.nextInt(8000 + 100)));
            highscores.add(new HighscoreEntry(s.getId(), "Petter", r.nextInt(8000 + 100)));
            highscores.add(new HighscoreEntry(s.getId(), "Ole", r.nextInt(8000 + 100)));
            highscores.add(new HighscoreEntry(s.getId(), "Hans", r.nextInt(8000 + 100)));
            highscores.add(new HighscoreEntry(s.getId(), "Tore", r.nextInt(8000 + 100)));
        }

        highscores.add(new HighscoreEntry(10, "HERO!", 9000));

        Log.d("HighscoreDB", "highscores " + highscores.size());

        sortList(highscores);

        for(HighscoreEntry h : highscores) {
            hdb.addHighscore(h);
        }
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
}
