package instant.moveadapt.com.backedupnotes.NotesContentProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by cristof on 16.08.2017.
 */

public class NotesDatabase extends SQLiteOpenHelper {
    public NotesDatabase(Context context) {
        super(context, NotesDatabaseContract.DATABASE_NAME, null, NotesDatabaseContract.DATBASE_VERSION);
    }


    public static final String CREATE_NOTES_TABLE_STATEMENT = "CREATE TABLE " + NotesDatabaseContract.Notite.URI + "(" +
            NotesDatabaseContract.Notite._ID + " integer primary key autoincrement, " +
            NotesDatabaseContract.Notite.COLUMN_NOTE + " text not null, " +
            NotesDatabaseContract.Notite.COLUMN_CREATE_TIMESTAMP + " integer, " +
            NotesDatabaseContract.Notite.COLUMN_MODIFIED + " text);";
    public static final String DROP_NOTES_TABLE_STATEMENT = "DROP TABLE IF EXISTS " + NotesDatabaseContract.Notite.TABLE_NAME;


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("NotesDatabase", CREATE_NOTES_TABLE_STATEMENT);
        db.execSQL(CREATE_NOTES_TABLE_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_NOTES_TABLE_STATEMENT);
        db.execSQL(CREATE_NOTES_TABLE_STATEMENT);
    }
}
