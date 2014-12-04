package no.hioa.sudokuapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import no.hioa.sudokuapp.R;
import no.hioa.sudokuapp.SudokuActivity;

/**
 * Created by Sondre on 26.11.2014.
 *
 * DialogFragment asks for a name for the highscore.
 */
public class HighscoreDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        final EditText input = new EditText(context);
        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_launcher)
                .setTitle(context.getResources().getString(R.string.highscore))
                .setView(input)
                .setPositiveButton(context.getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String name = input.getText().toString().trim();
                                ((SudokuActivity) getActivity()).addHighscore(name);
                            }
                        })
                .setNegativeButton(context.getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                ((SudokuActivity) getActivity()).newGameDialog();
                            }
                        }).create();
    }
}