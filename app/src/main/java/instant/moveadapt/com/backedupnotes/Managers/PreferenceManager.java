package instant.moveadapt.com.backedupnotes.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

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
        } else {
            ArrayList<Integer> states = new ArrayList<Integer>();
            states.add(newState);
            String statesJSON = gson.toJson(states);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.PREFERENCE_NOTES_STATES, statesJSON);
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


    public static void addUploadUriForFilename(Context context, String filename, Uri uri){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        Type hashMapType = new TypeToken<HashMap<String, String>>(){}.getType();
        String incompleteUploadsJSON = prefs.getString(Constants.PREFERENCE_INCOMPLETE_UPLOADS, "");
        HashMap<String, String> incompleteUploads = gson.fromJson(incompleteUploadsJSON, hashMapType);
        if (incompleteUploadsJSON != null && !incompleteUploadsJSON.equals("")){
            if (incompleteUploads != null){
                incompleteUploads.put(filename, uri.toString());
            }
            incompleteUploadsJSON = gson.toJson(incompleteUploads);
        } else {
            incompleteUploads = new HashMap<String, String>();
            incompleteUploads.put(filename, uri.toString());
        }
        incompleteUploadsJSON = gson.toJson(incompleteUploads);
        editor.putString(Constants.PREFERENCE_INCOMPLETE_UPLOADS, incompleteUploadsJSON);
        editor.commit();
    }

    public static Uri getIncompleteUploadUriByFilename(Context context, String filename){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String incompleteUploadsJSON = prefs.getString(Constants.PREFERENCE_INCOMPLETE_UPLOADS, "");
        Uri returnUri = null;
        Gson gson = new Gson();
        if (incompleteUploadsJSON != null && !incompleteUploadsJSON.equals("")){
            Type hashMapType = new TypeToken<HashMap<String, String>>(){}.getType();
            HashMap<String, String> incompleteUploads = gson.fromJson(incompleteUploadsJSON, hashMapType);
            if (incompleteUploads != null){
                String uriString = incompleteUploads.get(filename);
                if (uriString != null && !uriString.equals("")){
                    returnUri = Uri.parse(uriString);
                }
            }
        }
        return returnUri;
    }

    public static void deleteIncompleteUploadUriByFilename(Context context, String filename){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String incompleteUploadsJSON = prefs.getString(Constants.PREFERENCE_INCOMPLETE_UPLOADS, "");
        if (incompleteUploadsJSON != null && !incompleteUploadsJSON.equals("")){
            Type hashMapType = new TypeToken<HashMap<String, String>>(){}.getType();
            HashMap<String, String> incompleteUploads = gson.fromJson(incompleteUploadsJSON, hashMapType);
            incompleteUploads.remove(filename);
            incompleteUploadsJSON = gson.toJson(incompleteUploads);
            editor.putString(Constants.PREFERENCE_INCOMPLETE_UPLOADS, incompleteUploadsJSON);
            editor.commit();
        }
    }

    public static void addToBeDeletedFileFromCloud(Context context, String filename){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String toBeDeletedListJson = prefs.getString(Constants.PREFERENCE_TO_BE_DELETED, "");
        if (toBeDeletedListJson != null && !toBeDeletedListJson.equals("")){
            Type arrayListType = new TypeToken<ArrayList<String>>(){}.getType();
            ArrayList<String> toBeDeletedList = gson.fromJson(toBeDeletedListJson, arrayListType);
            toBeDeletedList.add(filename);
            toBeDeletedListJson = gson.toJson(toBeDeletedList);

        } else {
            ArrayList<String> toBeDeletedList = new ArrayList<String>();
            toBeDeletedListJson = gson.toJson(toBeDeletedList);
        }
        editor.putString(Constants.PREFERENCE_TO_BE_DELETED, toBeDeletedListJson);
        editor.commit();
    }

    public static void removeFromToBeDeletedFromCloud(Context context, String filename){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        Type arrayListType = new TypeToken<ArrayList<String>>(){}.getType();
        String toBeDeletedJSON = prefs.getString(Constants.PREFERENCE_TO_BE_DELETED, "");
        if (toBeDeletedJSON != null && toBeDeletedJSON.equals("")) {
            ArrayList<String> toBeDeleted = gson.fromJson(toBeDeletedJSON, arrayListType);
            toBeDeleted.remove(filename);
            toBeDeletedJSON = gson.toJson(toBeDeleted);
            editor.putString(Constants.PREFERENCE_TO_BE_DELETED, toBeDeletedJSON);
            editor.commit();
        }
    }

    public static boolean isToBeDeletedFromCloud(Context context, String filename){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        Type arrayListType = new TypeToken<ArrayList<String>>(){}.getType();
        String toBeDeletedJSON = prefs.getString(Constants.PREFERENCE_TO_BE_DELETED, "");
        if (toBeDeletedJSON != null && toBeDeletedJSON.equals("")) {
            ArrayList<String> toBeDeleted = gson.fromJson(toBeDeletedJSON, arrayListType);
            if (toBeDeleted.contains(filename))
                return true;
        }
        return false;
    }

    public static ArrayList<String> getFilesThatNeedToBeDeleted(Context context){
        SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<String> needToBeDeleted = null;
        String whatNeedsToBeDeletedJSON = prefs.getString(Constants.PREFERENCE_TO_BE_DELETED, "");
        Gson gson = new Gson();
        if (whatNeedsToBeDeletedJSON != null && !whatNeedsToBeDeletedJSON.equals("")){
            Type arrayListType = new TypeToken<ArrayList<String>>(){}.getType();
            needToBeDeleted = gson.fromJson(whatNeedsToBeDeletedJSON, arrayListType);
        }
        return needToBeDeleted;
    }
}
