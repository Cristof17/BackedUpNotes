package instant.moveadapt.com.backedupnotes.Database;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by cristof on 11.03.2018.
 */

public class NotesContentProvider extends ContentProvider {

    private  NotesDatabase db;


    @Override
    public boolean onCreate() {
        /**
         * null for db name
         * null for cursor factory
         * -1 for db version
         *
         * The NotesDatabase constructor takes care of everything
         */
       db = new NotesDatabase(getContext(), null, null, -1);
       return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase readableDB = db.getReadableDatabase();
        Cursor c = null;

        if (readableDB != null){
           c = readableDB.query(NotesDatabase.DatabaseContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return NotesDatabase.DatabaseContract.CONTENT_MIME_TYPE;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase writeableDB = db.getWritableDatabase();
        long lastId = -1;

        if (writeableDB != null){
            lastId = writeableDB.insert(NotesDatabase.DatabaseContract.TABLE_NAME, null, values);
        }
        /**
         * Don't think will use the return value of the content provider
         */
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase writeableDB = db.getWritableDatabase();
        int deletedRows = -1;
        if (writeableDB != null){
            deletedRows = writeableDB.delete(NotesDatabase.DatabaseContract.TABLE_NAME, selection, selectionArgs);
        }
        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase writeableDB = db.getWritableDatabase();
        int updatedRows = -1;

        if (writeableDB != null){
            updatedRows = writeableDB.update(NotesDatabase.DatabaseContract.TABLE_NAME, values, selection, selectionArgs);
        }
        return updatedRows;
    }
}
