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
        try {
            if (notesDatabase != null) {
                SQLiteDatabase readableDB = notesDatabase.getReadableDatabase();
                c = readableDB.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
            }
        }catch (Exception e){
            e.printStackTrace();
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
        long lastIdInserted = -1;
        if (tableName == null)
            return null;
        try {
            SQLiteDatabase writableDatabase = notesDatabase.getWritableDatabase();
            lastIdInserted = writableDatabase.insert(tableName, null, values);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (lastIdInserted == -1)
            return null;

        return Uri.withAppendedPath(NotesDatabaseContract.Notite.URI, lastIdInserted + "");

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableNameByUri(uri);
        long deletedRows = -1;
        if (tableName == null)
            return -1;
        try {
            SQLiteDatabase writableDatabase = notesDatabase.getWritableDatabase();
            deletedRows = writableDatabase.delete(tableName, selection, selectionArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
        return (int)deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = getTableNameByUri(uri);
        int rowsUpdated = -1;
        if (tableName == null)
            return -1;
        try {
            SQLiteDatabase writableDataase = notesDatabase.getWritableDatabase();
            rowsUpdated = writableDataase.update(tableName, values, selection, selectionArgs);
        }catch (Exception e){
            e.printStackTrace();
        }
        return rowsUpdated;
    }

    public String getTableNameByUri(Uri uri){
        if (uri.getLastPathSegment().equals(NotesDatabaseContract.Notite.TABLE_NAME)){
            return NotesDatabaseContract.Notite.TABLE_NAME;
        }
        return null;
    }
}
