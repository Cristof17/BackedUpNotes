package instant.moveadapt.com.backedupnotes.Managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import instant.moveadapt.com.backedupnotes.Constants;

/**
 * Created by cristof on 10.07.2017.
 */

public class PreferenceManager {

    public static boolean saveNotesStates(Context context, int notePosition, int newState){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String noteStatesString = prefs.getString(Constants.PREFERENCE_NOTES_STATES, "");
        if (noteStatesString == null || noteStatesString.equals(""))
            return false;
        Gson gson = new Gson();
        int[] states = gson.fromJson(noteStatesString, int[].class);
        if (states != null){
            states[notePosition] = newState;
            String statesJson = gson.toJson(states);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.PREFERENCE_NOTES_STATES, statesJson);
            return editor.commit();
        } else {
            return false;
        }
    }

    public static int[] getNotesStates(Context context){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String notesStatesString = prefs.getString(Constants.PREFERENCE_NOTES_STATES,"");
        if (notesStatesString == null || notesStatesString.equals(""))
            return null;
        int[] states = gson.fromJson(notesStatesString, int[].class);
        return states;
    }
}
