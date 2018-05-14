package instant.moveadapt.com.backedupnotes.Pojo;

import android.database.Cursor;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;

public class NoteManager {

    public static Note convertToNote(Cursor c){
        if (c == null){
            return null;
        }
        Note n = null;

        String text = c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract.COLUMN_TEXT));
        long timestamp = Long.parseLong(c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP)));
        String id = c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract._ID));
        n = new Note(id, text, timestamp);

        return n;
    }

}
