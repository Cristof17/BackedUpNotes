package instant.moveadapt.com.backedupnotes.Database;

import android.app.AlertDialog;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import instant.moveadapt.com.backedupnotes.NewNoteActivity;

/**
 * Created by cristof on 11.03.2018.
 */

public class NotesContentProviderContentObserver extends ContentObserver {

    private AlertDialog dialog;
    private ContentObserverCallback contentCallback;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public NotesContentProviderContentObserver(Handler handler, AlertDialog alertDialog, ContentObserverCallback contentCallback) {
        super(handler);
        this.dialog = alertDialog;
        this.contentCallback = contentCallback;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (dialog.isShowing()){
            dialog.dismiss();
            contentCallback.finished();
        }
        Log.d("Observer", "Changed");
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if (dialog.isShowing()){
            dialog.dismiss();
            contentCallback.finished();
        }
        Log.d("Observer", "Changed");
    }
}
