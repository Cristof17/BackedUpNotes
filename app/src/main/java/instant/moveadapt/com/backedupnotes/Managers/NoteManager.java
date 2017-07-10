package instant.moveadapt.com.backedupnotes.Managers;

import android.content.Context;
import android.preference.*;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import java.util.logging.LogManager;

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

    public static final int[] getNotesStates(Context context){
        return PreferenceManager.getNotesStates(context);
    }
}
