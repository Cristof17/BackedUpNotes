package instant.moveadapt.com.backedupnotes.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

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
        Type type = (new TypeToken<ArrayList<Integer>>(){}.getType());
        ArrayList<Integer> states = gson.fromJson(noteStatesString, type);
        if (states != null){
            states.set(notePosition, newState);
            String statesJson = gson.toJson(states);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.PREFERENCE_NOTES_STATES, statesJson);
            return editor.commit();
        } else {
            return false;
        }
    }

    public static ArrayList<Integer> getNotesStates(Context context){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String notesStatesString = prefs.getString(Constants.PREFERENCE_NOTES_STATES,"");
        if (notesStatesString == null || notesStatesString.equals(""))
            return null;
        Type arrayListType = new TypeToken<ArrayList<Integer>>(){}.getType();
        ArrayList<Integer> states = gson.fromJson(notesStatesString, arrayListType);
        return states;
    }

    public static void deleteStateForPosition(Context context, int position){
        boolean result = false;
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        String statesString = prefs.getString(Constants.PREFERENCE_NOTES_STATES, "");
        ArrayList<Integer> states = null;
        if (statesString != null && (!statesString.equals(""))){
            Type arrayType = new TypeToken<ArrayList<Integer>>(){}.getType();
            Gson gson = new Gson();
            states = gson.fromJson(statesString, arrayType);
            if (states != null){
                states.remove(position);
            }
            statesString = gson.toJson(states);
            editor.putString(Constants.PREFERENCE_NOTES_STATES, statesString);
            editor.commit();
        }
    }

    public static void addState(Context context, int newState){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String statesString = prefs.getString(Constants.PREFERENCE_NOTES_STATES, "");
        if ((statesString != null && !statesString.equals(""))){
            SharedPreferences.Editor editor = prefs.edit();
            Type arrayType = new TypeToken<ArrayList<Integer>>(){}.getType();
            ArrayList<Integer> states = gson.fromJson(statesString, arrayType);
            if (states != null){
                states.add(newState);
            }
            statesString = gson.toJson(states);
            editor.putString(Constants.PREFERENCE_NOTES_STATES, statesString);
            editor.commit();
        }
    }

    public static void setRemoteFolderCreationURI(Context context, Uri remoteUri) {
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        if (remoteUri != null)
            editor.putString(Constants.PREFERENCE_NOTE_FOLDER_SESSION_URI, remoteUri.toString());
        else
            editor.putString(Constants.PREFERENCE_NOTE_FOLDER_SESSION_URI, "");
        editor.commit();
    }

    public static Uri getRemoteFolderCreationUri(Context context){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String uriString = prefs.getString(Constants.PREFERENCE_NOTE_FOLDER_SESSION_URI, "");
        Uri sessionUri = null;
        if (uriString != null && (!uriString.equals(""))){
            sessionUri = Uri.parse(uriString);
        }
        return sessionUri;
    }


}
