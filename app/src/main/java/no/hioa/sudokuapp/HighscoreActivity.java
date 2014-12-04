package no.hioa.sudokuapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import no.hioa.sudokuapp.information.HighscoreEntry;
import no.hioa.sudokuapp.mysql.HighscoreSQLHelper;

/**
 * Created by Sondre on 26.11.2014.
 *
 * Contains methods for the highscore-list
 */
public class HighscoreActivity extends Activity {

    private int sudokuId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        setTitle(getResources().getString(R.string.title_activity_highscore));

        Intent intent = getIntent();
        sudokuId = intent.getIntExtra("sudokuid", 0);
        if(sudokuId == 0) Toast.makeText(this, getResources().getString(R.string.sudoku_notfound), Toast.LENGTH_SHORT).show();

        HighscoreSQLHelper db = new HighscoreSQLHelper(this);
        List<HighscoreEntry> highscores = db.getAllHighscoresFromSudoku(sudokuId);
        sortList(highscores);
        int limit = highscores.size();
        if(limit > 10) limit = 10;
        Log.d("highscoreActivity", "highscore size: " + highscores.size());

        for(int i = 0; i < limit; i++) {
            if(highscores.get(i) != null) addRow(highscores.get(i));
            else break;
        }
    }

    // sorts the list by highscore-score
    public void sortList(List<HighscoreEntry> list) {

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

    public void addRow(HighscoreEntry entry) {
        TableLayout table = (TableLayout) findViewById(R.id.tableLayout);

        TextView name = (TextView) LayoutInflater.from(this).inflate(R.layout.table_textview_name, null);
        TextView score = (TextView) LayoutInflater.from(this).inflate(R.layout.table_textview_score, null);
        name.setText(entry.getName());
        score.setText(entry.getScore()+"");

        TableRow row = new TableRow(this);

        row.setPadding(10,0,0,0);

        row.addView(name);
        row.addView(score);

        table.addView(row);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_only_exit, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        else if(id == R.id.exit_action) {
            exitDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void exit() {
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
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
}
