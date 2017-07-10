package instant.moveadapt.com.backedupnotes.Managers;

import android.content.Context;
import android.preference.*;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.LogManager;

import instant.moveadapt.com.backedupnotes.Constants;

/**
 * Created by cristof on 10.07.2017.
 */

public class NoteManager {

    private static final String TAG = "[NOTE_MANAGER]";

    public static final void setNoteState(Context context, int notePosition, int newState){
        boolean saved = instant.moveadapt.com.backedupnotes.Managers.PreferenceManager.saveNotesStates(context, notePosition, newState);
        if (saved){
            Log.d(TAG, "Modified state for note on position " + notePosition + " to " + newState);
        } else {
            Log.e(TAG, "Cannot modify state for note on positon " + notePosition + " to" + newState);
        }
    }

    public static final ArrayList<Integer> getNotesStates(Context context){
        return PreferenceManager.getNotesStates(context);
    }

    public static final int getNoteStateForIndex(Context context, int position){
        ArrayList<Integer> states = getNotesStates(context);
        if (states != null) {
            if (position < 0 || position >= states.size())
                return Constants.STATE_LOCAL;
            else
                return states.get(position);
        } else {
            return Constants.STATE_LOCAL;
        }
    }

    public static final void deleteNoteState(Context context, int position){
        ArrayList<Integer> states = PreferenceManager.getNotesStates(context);
        if (states != null){
            if (position < 0 || position >= states.size()) {
            }else {
                PreferenceManager.deleteStateForPosition(context, position);
            }
        }
    }

    public static void addNoteState(Context context, int newState){
        PreferenceManager.addState(context, newState);
    }

    public static File addNote(Context context){
        File newNote = FileManager.createNewNoteFile(context);
        addNoteState(context, Constants.STATE_LOCAL);
        return newNote;
    }
}
