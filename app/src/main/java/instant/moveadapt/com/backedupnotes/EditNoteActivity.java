package instant.moveadapt.com.backedupnotes;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import instant.moveadapt.com.backedupnotes.Database.ContentObserverCallback;
import instant.moveadapt.com.backedupnotes.Database.NotesContentProviderContentObserver;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Pojo.Note;

/**
 * Created by cristof on 11.03.2018.
 */

public class EditNoteActivity extends AppCompatActivity implements ContentObserverCallback{

    EditText noteTextView;
    Note note;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_note_layout);
        toolbar = (Toolbar) findViewById(R.id.activity_new_note_toolbar);
        ((AppCompatActivity)this).setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        noteTextView = (EditText) findViewById(R.id.new_note_edit_text);

        /*
         * Get the note
         */
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            note = extras.getParcelable("note");
        }else{
            Toast.makeText(EditNoteActivity.this, "Error with the app; Cannot edit note", Toast.LENGTH_LONG).show();
            finish();
        }

        noteTextView.setText(note.text);
    }

    @Override
    public void onBackPressed() {

        /**
         * Show dialog with save the note
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(EditNoteActivity.this);
        builder.setTitle(R.string.save_note_text);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes_text), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                    /*
                     * Show waiting dialog
                     */
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditNoteActivity.this);
                    builder.setTitle(R.string.saving_text);
                    builder.setCancelable(false);
                    AlertDialog savingDialog = builder.create();
                    savingDialog.show();
                    getContentResolver().registerContentObserver(NotesDatabase.DatabaseContract.URI, true, new NotesContentProviderContentObserver(new Handler(), savingDialog, EditNoteActivity.this));

                if (noteTextView.getText().toString() != null
                        && !noteTextView.getText().toString().equals("")) {
                    /*
                     * Update the new Note
                     */
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(NotesDatabase.DatabaseContract._ID, note.id.toString());
                    contentValues.put(NotesDatabase.DatabaseContract.COLUMN_TEXT, noteTextView.getText().toString());
                    contentValues.put(NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP, System.currentTimeMillis());
                    String whereClause = NotesDatabase.DatabaseContract._ID + " = ? ";
                    String whereArgs[] = new String[]{note.id.toString()};
                    getContentResolver().update(NotesDatabase.DatabaseContract.URI, contentValues, whereClause, whereArgs);
                }else{

                    /*
                     * Delete the note
                     */
                    String whereClause = NotesDatabase.DatabaseContract._ID + " = ? ";
                    String whereArgs[] = new String[]{note.id.toString()};
                    getContentResolver().delete(NotesDatabase.DatabaseContract.URI, whereClause, whereArgs);
                }
            }
        });

        builder.setNegativeButton(R.string.no_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditNoteActivity.super.onBackPressed();
                Log.d("notes.db", "Do not save note");
            }
        });
        builder.create().show();
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
        EditNoteActivity.super.onBackPressed();
    }
}
