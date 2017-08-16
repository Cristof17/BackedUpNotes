package instant.moveadapt.com.backedupnotes.Managers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.*;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.LogManager;

import instant.moveadapt.com.backedupnotes.Constants;
import instant.moveadapt.com.backedupnotes.NotesContentProvider.NotesDatabaseContract;
import instant.moveadapt.com.backedupnotes.Notita;

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

    public static void setNoteStateByName(Context context, String name, int newState){
        int fileIndex = FileManager.getFileIndexByName(context, name);
        if (fileIndex != -1){
            NoteManager.setNoteState(context, fileIndex, newState);
        }
    }

    public static Uri getUploadUriByFilename(Context context, String filename){
        Uri returnUri = null;
        if (filename != null && !filename.equals("")){
            returnUri = PreferenceManager.getIncompleteUploadUriByFilename(context, filename);
        }
        return returnUri;
    }

    public static void completeUploadByFilename(Context context, String filename){
        if (filename != null && !filename.equals("")){
            PreferenceManager.deleteIncompleteUploadUriByFilename(context, filename);
        }
    }

    public static void setUriByFilename(Context context, String filename, Uri uri){
        if (filename != null && !filename.equals("")) {
            PreferenceManager.addUploadUriForFilename(context, filename, uri);
        }
    }

    public static void addToBeDeletedFromCloud(Context context, String filename){
        if (filename != null && !filename.equals("")){
            PreferenceManager.addToBeDeletedFileFromCloud(context, filename);
        }
    }

    public static void removeToBeDeletedFromCloud(Context context, String filename){
        if (filename != null && !filename.equals("")){
            PreferenceManager.removeFromToBeDeletedFromCloud(context, filename);
        }
    }

    public static boolean isToBeDeletedFromCloud(Context context, String filename){
        boolean returnVal = false;
        if (filename != null && !filename.equals("")){
            return PreferenceManager.isToBeDeletedFromCloud(context,filename);
        }
        return false;
    }

    public static ArrayList<String> getWhatNeedsToBeDeleted(Context context){
        ArrayList<String> needsToBeDeleted = PreferenceManager.getFilesThatNeedToBeDeleted(context);
        return needsToBeDeleted;
    }

    public static ArrayList<Notita> getNotesFromDatabase(Context context){
        ArrayList<Notita> notite = new ArrayList<Notita>();
        ContentResolver resolver = context.getContentResolver();
        String[] columns = getNotiteTableColumns();
        Cursor c = resolver.query(NotesDatabaseContract.Notite.URI,
                columns,
                null,
                null,
                null);
        if (c == null || c.getCount() == 0){
            c.close();
            return null;
        }

        int numNotes = c.getCount();
        for (int i = 0; i < numNotes; ++i){
            c.moveToNext();
            Notita notitaNoua = convertNotita(c);
            notite.add(notitaNoua);
        }
        c.close();
        return notite;
    }

    public static String[] getNotiteTableColumns(){
        return new String[] {NotesDatabaseContract.Notite._ID,
                NotesDatabaseContract.Notite.COLUMN_MODIFIED,
                NotesDatabaseContract.Notite.COLUMN_NOTE,
                NotesDatabaseContract.Notite.COLUMN_CREATE_TIMESTAMP,
                NotesDatabaseContract.Notite.COLUMN_MODIFIED_TIMESTAMP};
    }

    public static Notita convertNotita(@Nullable  Cursor c){
        int id = Integer.parseInt(c.getString(c.getColumnIndex(NotesDatabaseContract.Notite._ID)));
        long createTimestamp = Long.parseLong(c.getString(c.getColumnIndex(NotesDatabaseContract.Notite.COLUMN_CREATE_TIMESTAMP)));
        long modifyTimestamp = Long.parseLong(c.getString(c.getColumnIndex(NotesDatabaseContract.Notite.COLUMN_MODIFIED_TIMESTAMP)));
        boolean modified = Boolean.parseBoolean(c.getString(c.getColumnIndex(NotesDatabaseContract.Notite.COLUMN_MODIFIED)));
        String notita = c.getString(c.getColumnIndex(NotesDatabaseContract.Notite.COLUMN_NOTE));
        return new Notita(id, createTimestamp, modifyTimestamp, modified, notita);
    }

}
