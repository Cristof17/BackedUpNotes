package instant.moveadapt.com.backedupnotes.Database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.TabHost;


/**
 * Created by cristof on 11.03.2018.
 */

public class NotesDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 3;
    private static final String CREATE_STATEMENT = new String("CREATE TABLE "
            + DatabaseContract.TABLE_NAME
            + "("
            + DatabaseContract._ID
            + " text primary key, "
            + DatabaseContract.COLUMN_TEXT
            + " text not null, "
            + DatabaseContract.COLUMN_TIMESTAMP
            + " date not null);");

    private static final String CREATE_TABLE_TO_BE_DELETED_STATEMENT = new String ("CREATE TABLE "
            + DeleteNotesContract.TABLE_NAME
            + "("
            + DeleteNotesContract._ID
            + " text primary key, "
            + DeleteNotesContract.COLUMN_TEXT
            + " text not null, "
            + DeleteNotesContract.COLUMN_TIMESTAMP
            + " date not null);");

    private static final String DROP_STATEMENT = "DROP TABLE " + DatabaseContract.TABLE_NAME + ";";
    private static final String DROP_STATEMENT_DELETE = "DROP TABLE " + DatabaseContract.TABLE_NAME + ";";

    public NotesDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STATEMENT);
        db.execSQL(CREATE_TABLE_TO_BE_DELETED_STATEMENT);
        Log.d(DATABASE_NAME, "onCreate()");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_STATEMENT);
        db.execSQL(CREATE_STATEMENT);
        db.execSQL(DROP_STATEMENT);
        db.execSQL(CREATE_TABLE_TO_BE_DELETED_STATEMENT);
        Log.d(DATABASE_NAME, "onUpgrade()");
    }

    public static class DatabaseContract implements BaseColumns {

        /**
         * Columns of the database
         */
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_TIMESTAMP = "trimestamp";

        static final String AUTHORITY = "instant.moveadapt.com.backedupnotes";
        static final String CONTENT_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + AUTHORITY;
        public static final Uri URI = Uri.parse("content://" + AUTHORITY + "." + TABLE_NAME);

        public static String[] getTableColumns() {
            String tableColumns[] = new String[]{_ID, COLUMN_TEXT, COLUMN_TIMESTAMP};
            return tableColumns;
        }
    }

    public static class DeleteNotesContract implements BaseColumns {
        public static final String TABLE_NAME = "toBeDeleted";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_TIMESTAMP = "trimestamp";

        static final String AUTHORITY = "instant.moveadapt.com.backedupnotes";
        static final String CONTENT_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + AUTHORITY;
        public static final Uri URI = Uri.parse("content://" + AUTHORITY + "." + TABLE_NAME);

        public static String[] getTableColumns() {
            String tableColumns[] = new String[]{_ID, COLUMN_TEXT, COLUMN_TIMESTAMP};
            return tableColumns;
        }
    }
}
