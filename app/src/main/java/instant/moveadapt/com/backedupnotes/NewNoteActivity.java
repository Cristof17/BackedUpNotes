package instant.moveadapt.com.backedupnotes;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.util.UUID;

import instant.moveadapt.com.backedupnotes.Database.ContentObserverCallback;
import instant.moveadapt.com.backedupnotes.Database.NotesContentProvider;
import instant.moveadapt.com.backedupnotes.Database.NotesContentProviderContentObserver;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;

/**
 * Created by cristof on 11.03.2018.
 */

public class NewNoteActivity extends AppCompatActivity implements ContentObserverCallback{

    private EditText textEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_note_layout);
        textEditText = (EditText) findViewById(R.id.new_note_edit_text);

    }

    @Override
    public void onBackPressed() {

        if (textEditText.getText().toString() != null
                && !textEditText.getText().toString().equals("")) {

            /**
             * Show dialog with save the note
             */
            AlertDialog.Builder builder = new AlertDialog.Builder(NewNoteActivity.this);
            builder.setTitle(R.string.save_note_text);
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.yes_text), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    /*
                     * Show waiting dialog
                     */
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewNoteActivity.this);
                    builder.setTitle(R.string.saving_text);
                    builder.setCancelable(false);
                    AlertDialog savingDialog = builder.create();
                    savingDialog.show();
                    getContentResolver().registerContentObserver(NotesDatabase.DatabaseContract.URI, true, new NotesContentProviderContentObserver(new Handler(), savingDialog, NewNoteActivity.this));
                    /*
                     * Insert the new note in the database
                     */
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(NotesDatabase.DatabaseContract._ID, UUID.randomUUID().toString());
                    contentValues.put(NotesDatabase.DatabaseContract.COLUMN_TEXT, textEditText.getText().toString());
                    contentValues.put(NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP, System.currentTimeMillis());
                    getContentResolver().insert(NotesDatabase.DatabaseContract.URI, contentValues);
                }
            });

            builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NewNoteActivity.super.onBackPressed();
                    Log.d("notes.db", "Do not save note");
                }
            });
            builder.create().show();
        }else{
            super.onBackPressed();
        }
    }

    /**
     * Callback method called by the
     * custom ContentProviderObserver
     */
    @Override
    public void finished() {
         /*
         * Resume normal behavior of the app after the
         * data has been saved in the content provideer
         */
        NewNoteActivity.super.onBackPressed();
    }
}
