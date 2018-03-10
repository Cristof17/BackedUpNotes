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

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_STATEMENT = new String("CREATE TABLE "
            + DatabaseContract.TABLE_NAME
            + "("
            + DatabaseContract._ID
            + " text primary key not null, "
            + DatabaseContract.COLUMN_TEXT
            + " text not null, "
            + DatabaseContract.COLUMN_TIMESTAMP
            + " date not null);");

    private static final String DROP_STATEMENT = "DROP TABLE " + DatabaseContract.TABLE_NAME + ";";

    public NotesDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STATEMENT);
        Log.d(DATABASE_NAME, "onCreate()");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_STATEMENT);
        db.execSQL(CREATE_STATEMENT);
        Log.d(DATABASE_NAME, "onUpgrade()");
    }

    public static class DatabaseContract implements BaseColumns{

        /**
         * Columns of the database
         */
        static final String TABLE_NAME = "notes";
        static final String COLUMN_TEXT = "text";
        static final String COLUMN_TIMESTAMP = "trimestamp";

        static final String AUTHORITY = "instant.moveadapt.com.backedupnotes";
        static final String CONTENT_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + AUTHORITY;
        static final Uri URI = Uri.parse("content://" + AUTHORITY);
    }
}
