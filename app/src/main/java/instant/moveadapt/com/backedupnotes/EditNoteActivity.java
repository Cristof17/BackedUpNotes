package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.security.Permission;
import java.util.concurrent.ExecutionException;

import instant.moveadapt.com.backedupnotes.Constants;
import instant.moveadapt.com.backedupnotes.Managers.FileManager;
import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.Managers.PreferenceManager;
import instant.moveadapt.com.backedupnotes.R;
import instant.moveadapt.com.backedupnotes.RecyclerView.NewNoteActivity;

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
                file = FileManager.getFileForIndex(EditNoteActivity.this, position);
                StringBuilder noteString = new StringBuilder();
                try {
                    BufferedReader fileReader = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = fileReader.readLine()) != null){
                        noteString.append(line);
                    }
                    editText.setText(noteString.toString());

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        if (ContextCompat.checkSelfPermission(EditNoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(EditNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_WRITE_PERMISSION_REQ_CODE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        /*
            Save the note
         */
        if (file != null){
            if (editText.getText().toString() != null && !editText.getText().toString().equals("")) {
//                try {
//                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//                    if (editText != null) {
//                        writer.write(editText.getText().toString());
//                    }
//                    writer.flush();
//                    writer.close();
//                    Log.d(TAG, "Text written in file");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                Resources resources = getResources();
                String title = resources.getString(R.string.save_file_progress_title);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditNoteActivity.this);
                alertDialogBuilder.setTitle(title);
                LayoutInflater inflater = (LayoutInflater)EditNoteActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //same view as alert dialog show while uploading files
                View alertDialogView = inflater.inflate(R.layout.file_upload_progress, null, false);
                ProgressBar progressBar = (ProgressBar) alertDialogView.findViewById(R.id.bytes_upload_progress_layout_files_progressbar);
                alertDialogBuilder.setView(alertDialogView);
                AlertDialog saveFileDialog = alertDialogBuilder.create();
                SaveFileAsyncTask saveFileTask = new SaveFileAsyncTask(EditNoteActivity.this, file,  saveFileDialog, progressBar, editText.getText().toString());
                try {
                    saveFileTask.execute().get();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }catch(ExecutionException e){
                    e.printStackTrace();
                }
            } else {
                file.delete();
            }

            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager.getActiveNetworkInfo() != null && NoteManager.getNoteStateForIndex(EditNoteActivity.this, position) == Constants.STATE_GLOBAL){
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference bucket = storage.getReference();
                StorageReference notesFolder = bucket.child(Constants.REMOTE_NOTE_FOLDER);
                StorageReference toBeDeletedFile = notesFolder.child(file.getName());
                Task deleteTask = toBeDeletedFile.delete();
                deleteTask.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        Log.d(TAG, "Deleted successfully " + file.getName());
                    }
                });
                deleteTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        handleTaskException((StorageException)e);
                    }
                });

            } else {
                NoteManager.addToBeDeletedFromCloud(EditNoteActivity.this, file.getName());
            }
            NoteManager.setNoteState(EditNoteActivity.this, position, Constants.STATE_LOCAL);
        }
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

    private static class SaveFileAsyncTask extends AsyncTask<Void, Void, Void> {

        private File file;
        private AlertDialog progressAlertDialog;
        private int offset = 0;
        private ProgressBar progressBar;
        private String editTexttext;
        private Context context;

        public SaveFileAsyncTask(Context context, File file, AlertDialog progressAlertDialog, ProgressBar progressBar, String editTexttext){
            this.file = file;
            this.progressAlertDialog = progressAlertDialog;
            this.progressBar = progressBar;
            this.editTexttext = editTexttext;
            this.context = context;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setMax((int)file.length());
            progressAlertDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                BufferedReader reader = new BufferedReader(new StringReader(editTexttext));
                int charInt = reader.read();
                while (charInt != -1){
                    writer.append((char)charInt);
                    charInt = reader.read();
                    offset++;
                }
                writer.flush();
                writer.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e ){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(offset);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressAlertDialog.dismiss();
        }
    }
}
