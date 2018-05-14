package instant.moveadapt.com.backedupnotes.Database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import instant.moveadapt.com.backedupnotes.Pojo.Note;

public class DatabaseManager {

    public static void saveNoteLocally(Context context, Note note) {
        if (note == null)
            return;

        ContentResolver contentResolver = context.getContentResolver();
        ContentValues vals = new ContentValues();
        vals.put(NotesDatabase.DatabaseContract._ID, note.id.toString());
        vals.put(NotesDatabase.DatabaseContract.COLUMN_TEXT, note.text);
        vals.put(NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP, note.timestamp);
        contentResolver.insert(NotesDatabase.DatabaseContract.URI, vals);
    }
}
