package instant.moveadapt.com.backedupnotes.NotesContentProvider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by cristof on 16.08.2017.
 */

public class NotesDatabaseContract {

    public static final String DATABASE_NAME = "notite";
    public static final int DATBASE_VERSION = 1;

    public static final String AUTHORITY = "instant.moveadapt.com.backedupnotes";
    public static final Uri URI = Uri.parse("content://" + AUTHORITY);
    public static final String TABLE_NOTES = "notes";

    public static final class Notite implements BaseColumns{
        public static final String TABLE_NAME = TABLE_NOTES;
        public static final Uri URI = Uri.withAppendedPath(NotesDatabaseContract.URI, TABLE_NAME);
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_CREATE_TIMESTAMP = "create_timestamp";
        public static final String COLUMN_MODIFIED_TIMESTAMP = "modofied_timestamp";
        public static final String COLUMN_MODIFIED = "modified";
        public static final String CONTENT_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + NotesDatabaseContract.AUTHORITY;
    }


}
