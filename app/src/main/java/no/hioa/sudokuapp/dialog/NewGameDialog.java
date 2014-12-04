package no.hioa.sudokuapp.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import no.hioa.sudokuapp.R;
import no.hioa.sudokuapp.SudokuActivity;

/**
 * Created by Sondre on 26.11.2014.
 *
 * DialogFragment that asks user if they want a new game
 */
public class NewGameDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        ((SudokuActivity) getActivity()).nextGame();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        Context context = getActivity();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(context.getResources().getString(R.string.new_game))
                .setMessage(context.getResources().getString(R.string.new_game_dialog))
                .setPositiveButton(context.getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(context.getResources().getString(R.string.no), dialogClickListener);

        return alertDialogBuilder.create();
    }
}