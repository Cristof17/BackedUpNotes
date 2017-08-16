package instant.moveadapt.com.backedupnotes.NotesContentProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by cristof on 16.08.2017.
 */

public class NotesContentProvider extends ContentProvider {

    private NotesDatabase notesDatabase;

    @Override
    public boolean onCreate() {
        notesDatabase = new NotesDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String tableName = getTableNameByUri(uri);
        if (tableName == null)
            return null;
        Cursor c = null;
        if (notesDatabase != null){
            SQLiteDatabase readableDB = notesDatabase.getReadableDatabase();
            c = readableDB.query(tableName, projection,selection, selectionArgs, null, null, sortOrder);
        }
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return NotesDatabaseContract.Notite.CONTENT_MIME_TYPE;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String tableName = getTableNameByUri(uri);
        if (tableName == null)
            return null;
        SQLiteDatabase writableDatabase = notesDatabase.getWritableDatabase();
        long lastIdInserted = writableDatabase.insert(tableName, null, values);
        if (lastIdInserted == -1)
            return null;

        return Uri.withAppendedPath(NotesDatabaseContract.Notite.URI, lastIdInserted + "");

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableNameByUri(uri);
        if (tableName == null)
            return -1;
        SQLiteDatabase writableDatabase = notesDatabase.getWritableDatabase();
        long deletedRows = writableDatabase.delete(tableName,selection, selectionArgs);
        return (int)deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableNameByUri(uri);
        if (tableName == null)
            return -1;
        SQLiteDatabase writableDataase = notesDatabase.getWritableDatabase();
        int rowsUpdated = writableDataase.update(tableName,values, selection, selectionArgs);
        return rowsUpdated;
    }

    public String getTableNameByUri(Uri uri){
        if (uri.getLastPathSegment().equals(NotesDatabaseContract.Notite.TABLE_NAME)){
            return NotesDatabaseContract.Notite.TABLE_NAME;
        }
        return null;
    }
}
