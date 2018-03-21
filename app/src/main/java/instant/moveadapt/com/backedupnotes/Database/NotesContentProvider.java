package instant.moveadapt.com.backedupnotes.Database;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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

        if (uri.equals( NotesDatabase.DatabaseContract.URI)){
            c = readableDB.query(NotesDatabase.DatabaseContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            /*
             * If a componenet requests for this cursor, the cursor needs to
             * have a listener attached for when the underlying data changes
             */
            c.setNotificationUri(getContext().getContentResolver(), NotesDatabase.DatabaseContract.URI);
        } else if (uri.equals(NotesDatabase.DeleteNotesContract.URI)){
            c = readableDB.query(NotesDatabase.DeleteNotesContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            /*
             * If a componenet requests for this cursor, the cursor needs to
             * have a listener attached for when the underlying data changes
             */
            c.setNotificationUri(getContext().getContentResolver(), NotesDatabase.DeleteNotesContract.URI);
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

            /**
             * Check if it needs to be inserted in the deleteTable or
             * notesTable
             */


            if (uri.equals(NotesDatabase.DatabaseContract.URI)){
                lastId = writeableDB.insert(NotesDatabase.DatabaseContract.TABLE_NAME, null, values);
                Log.d(NotesDatabase.DATABASE_NAME, " inserted with last Id = " + lastId);

                /*
                 * Notify registered contentObserver objects about the modifications of the
                 * underlying data
                 */
                getContext().getContentResolver().notifyChange(NotesDatabase.DatabaseContract.URI, null);
            } else if (uri.equals(NotesDatabase.DeleteNotesContract.URI)){
                lastId = writeableDB.insert(NotesDatabase.DeleteNotesContract.TABLE_NAME, null, values);
                Log.d(NotesDatabase.DATABASE_NAME, " inserted with last Id = " + lastId);

                /*
                 * Notify registered contentObserver objects about the modifications of the
                 * underlying data
                 */
                getContext().getContentResolver().notifyChange(NotesDatabase.DeleteNotesContract.URI, null);
            }

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

            /**
             * Check if it needs to be inserted in the deleteTable or
             * notesTable
             */
            if (uri.equals(NotesDatabase.DatabaseContract.URI)){
                deletedRows = writeableDB.delete(NotesDatabase.DatabaseContract.TABLE_NAME, selection, selectionArgs);

            /*
             * Notify registered contentObserver objects about the modifications of the
             * underlying data
             */
                getContext().getContentResolver().notifyChange(NotesDatabase.DatabaseContract.URI, null);
            } else if (uri.equals(NotesDatabase.DeleteNotesContract.URI)){
                deletedRows = writeableDB.delete(NotesDatabase.DeleteNotesContract.TABLE_NAME, selection, selectionArgs);

            /*
             * Notify registered contentObserver objects about the modifications of the
             * underlying data
             */
                getContext().getContentResolver().notifyChange(NotesDatabase.DeleteNotesContract.URI, null);
            }
        }
        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase writeableDB = db.getWritableDatabase();
        int updatedRows = -1;

        if (writeableDB != null){
            /**
             * Check if it needs to be inserted in the deleteTable or
             * notesTable
             */
            if (uri.equals(NotesDatabase.DatabaseContract.URI)){
                updatedRows = writeableDB.update(NotesDatabase.DatabaseContract.TABLE_NAME, values, selection, selectionArgs);

            /*
             * Notify registered contentObserver objects about the modifications of the
             * underlying data
             */
                getContext().getContentResolver().notifyChange(NotesDatabase.DatabaseContract.URI, null);
            } else if (uri.equals(NotesDatabase.DeleteNotesContract.URI)){
                updatedRows = writeableDB.update(NotesDatabase.DeleteNotesContract.TABLE_NAME, values, selection, selectionArgs);

            /*
             * Notify registered contentObserver objects about the modifications of the
             * underlying data
             */
                getContext().getContentResolver().notifyChange(NotesDatabase.DeleteNotesContract.URI, null);
            }
        }
        return updatedRows;
    }
}
