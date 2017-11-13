package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.storage.StorageException;

import java.io.File;
import java.util.Calendar;

import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.NotesContentProvider.NotesDatabaseContract;

/**
 * Created by cristof on 13.06.2017.
 */

public class EditNoteActivity extends AppCompatActivity {

    public static final String TAG = "[EDIT_NOTE_ACTIVITY]";

    private Toolbar toolbar;
    private EditText editText;
    private File file;
    private Intent intent;
    private int position;
    private int touchCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_new_note_layout);
        toolbar = (Toolbar)findViewById(R.id.new_note_toolbar);
        editText = (EditText)findViewById(R.id.new_note_edit_text);

        if ((intent = getIntent()) != null){
            position = intent.getIntExtra(Constants.INTENT_EDIT_FILE_POSITION, -1);
            if (position == -1){
                String message = getResources().getString(R.string.cannot_edit_note);
                Toast.makeText(EditNoteActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                ContentResolver resolver = getContentResolver();
                String[] notesColumns = NoteManager.getNotiteTableColumns();
                String whereClause = NotesDatabaseContract.Notite._ID + " = ? ";
                String whereArgs[] = {position+ ""};
                Cursor c = resolver.query(NotesDatabaseContract.Notite.URI,
                        notesColumns,
                        whereClause,
                        whereArgs,
                        null);
                if (c == null || c.getCount() == 0) {
                    String message = getResources().getString(R.string.cannot_edit_note);
                    Toast.makeText(EditNoteActivity.this, message, Toast.LENGTH_SHORT).show();
                    c.close();
                    finish();
                }

                c.moveToNext();
                Notita notita = NoteManager.convertNotita(c);
                editText.setText(notita.getNote().toString());
            }
        }

        if (ContextCompat.checkSelfPermission(EditNoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(EditNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_WRITE_PERMISSION_REQ_CODE);
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackButtonPressed()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        /*
            Save the note
         */
        ContentResolver resolver = getContentResolver();
        ContentValues vals = new ContentValues();
        String whereClause = NotesDatabaseContract.Notite._ID + " = ? ";
        String[] whereArgs = {position + ""};
        vals.put(NotesDatabaseContract.Notite.COLUMN_NOTE, editText.getText().toString());
        vals.put(NotesDatabaseContract.Notite.COLUMN_MODIFIED_TIMESTAMP, Calendar.getInstance().getTimeInMillis());
        vals.put(NotesDatabaseContract.Notite.COLUMN_MODIFIED, "true");
        int updated = resolver.update(NotesDatabaseContract.Notite.URI,
                vals,
                whereClause,
                whereArgs);
        Log.d(TAG, "Rows updated = " + updated);

            /*
                To be deleted from cloud as soon as it is edited
             */

//            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
//            if (connectivityManager.getActiveNetworkInfo() != null && NoteManager.getNoteStateForIndex(EditNoteActivity.this, position) == Constants.STATE_GLOBAL){
//                FirebaseStorage storage = FirebaseStorage.getInstance();
//                StorageReference bucket = storage.getReference();
//                StorageReference notesFolder = bucket.child(Constants.REMOTE_NOTE_FOLDER);
//                StorageReference toBeDeletedFile = notesFolder.child(file.getName());
//                Task deleteTask = toBeDeletedFile.delete();
//                deleteTask.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        Log.d(TAG, "Deleted successfully " + file.getName());
//                    }
//                });
//                deleteTask.addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        handleTaskException((StorageException)e);
//                    }
//                });
//
//            } else {
//                NoteManager.addToBeDeletedFromCloud(EditNoteActivity.this, file.getName());
//            }
//            NoteManager.setNoteState(EditNoteActivity.this, position, Constants.STATE_LOCAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.READ_WRITE_PERMISSION_REQ_CODE){
            int result = grantResults[0];
            if (result == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Permission for rd/wr to external storage is granted");
            } else {
                Log.d(TAG, "Permission for rd/wr to external storage is denied");
            }
        }
    }

    public void handleTaskException(StorageException storageException){
        switch (storageException.getErrorCode()) {
            case StorageException.ERROR_BUCKET_NOT_FOUND: {
                Toast.makeText(EditNoteActivity.this, "Bucket not found", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Bucket not found");
                break;
            }
            case StorageException.ERROR_NOT_AUTHORIZED: {
                Toast.makeText(EditNoteActivity.this, "Not authorized to access the server folder", Toast.LENGTH_LONG).show();
                Log.d(TAG,"Not authorized to access the server folder");
                break;
            }
            case StorageException.ERROR_UNKNOWN: {
                Toast.makeText(EditNoteActivity.this, "An unknown error occured", Toast.LENGTH_LONG).show();
                Log.d(TAG,"An unknown error occured");
                break;
            }
            case StorageException.ERROR_OBJECT_NOT_FOUND: {
                Toast.makeText(EditNoteActivity.this, "Server folder does not exist", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Server folder does not exist");
                break;
            }
            case StorageException.ERROR_PROJECT_NOT_FOUND: {
                Toast.makeText(EditNoteActivity.this, "Remote server project not found", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Remote server project not found");
                break;
            }
        }
    }
}
