package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import instant.moveadapt.com.backedupnotes.ActionMode.ActionModeMonitor;
import instant.moveadapt.com.backedupnotes.Managers.FileManager;
import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.Managers.PreferenceManager;
import instant.moveadapt.com.backedupnotes.RecyclerView.NewNoteActivity;
import instant.moveadapt.com.backedupnotes.RecyclerView.NoteListRecyclerViewAdapter;

public class NotesList extends AppCompatActivity implements ActionMode.Callback{

    private static final int INTERNET_PERMISSION_REQUEST_CODE = 101;

    private FloatingActionButton addButton;
    private RecyclerView notesList;
    private NoteListRecyclerViewAdapter notesListRecyclerViewAdapter;
    private TextView errorTextView;//used for when t
    // he permission is not granted to show to the user
    //she cannot use the app
    private LinearLayoutManager llm;
    private static final String TAG = "[NOTE_LIST]";

    private StorageMetadata returnMetadata;
    private int fileIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
        /*
            Bind Views
         */
        addButton = (FloatingActionButton)findViewById(R.id.add_note_button);
        errorTextView = (TextView)findViewById(R.id.error_text_view);
        notesList = (RecyclerView)findViewById(R.id.recycler_view);

         /*
            Request permission for android 6.0 and upwards
         */
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(NotesList.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_WRITE_PERMISSION_REQ_CODE);
        } else {
            /*
                Create the folder where to store notes
             */
            FileManager.createNotesFolder(this);
        }

        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewNoteActivity.class);
                startActivity(intent);
            }
        });

        notesListRecyclerViewAdapter = new NoteListRecyclerViewAdapter(this, notesList, this, this);
        llm = new LinearLayoutManager(this);
        notesList.setAdapter(notesListRecyclerViewAdapter);
        notesList.setLayoutManager(llm);
        notesList.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.READ_WRITE_PERMISSION_REQ_CODE){
            int result = grantResults[0];
            if (result == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Permission for rd/wr to external storage is granted");
                /*
                    Create the folder where to store notes
                */
                FileManager.createNotesFolder(this);
            } else {
                Log.d(TAG, "Permission for rd/wr to external storage is denied");
                if (errorTextView !=  null && addButton != null && notesList != null){
                    showPermissionErrorText();
                } else {
                    finish();
                }
            }
        }

        if (requestCode == INTERNET_PERMISSION_REQUEST_CODE){
            int result = grantResults[0];
            if (result == PackageManager.PERMISSION_GRANTED){
                Resources res = getResources();
                String permissionGrantedMessage = res.getString(R.string.permission_granted_message);
                Toast.makeText(NotesList.this, permissionGrantedMessage, Toast.LENGTH_LONG).show();
            } else {
                Resources resources = getResources();
                String noPermissionWarning = resources.getString(R.string.cannot_upload_warning);
                Toast.makeText(NotesList.this, noPermissionWarning, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        notesListRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.note_list_menu, menu);
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_list_contextual_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        ActionModeMonitor.setSelected(true);
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()){
            case R.id.note_list_contextual_delete_action:
            {
                ArrayList<Integer> toDelete = new ArrayList<Integer>();
                //delete the notes which have been selected
                for (int i = 0; i < FileManager.getNumNotes(NotesList.this); ++i) {
                    if (ActionModeMonitor.getActivated(i)) {
                        ActionModeMonitor.deleteActivated(i);
                        NoteManager.deleteNoteState(NotesList.this, i);
                        File toBeDeleted = FileManager.getFileForIndex(NotesList.this, i);
                        FileManager.deleteFile(NotesList.this,i);
                        ConnectivityManager connectionManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectionManager.getActiveNetworkInfo() != null){
                            deleteCloudFile(toBeDeleted.getName());
                        } else {
                            NoteManager.addToBeDeletedFromCloud(NotesList.this, toBeDeleted.getName());
                        }
                        notesListRecyclerViewAdapter.notifyItemRemoved(i);
                        --i;
                    }
                }
                return true;
            }
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        for (int i = 0; i < notesList.getAdapter().getItemCount(); ++i){
            ActionModeMonitor.setActivated(i, false);
        }
        ActionModeMonitor.setSelected(false);
        notesList.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.note_list_menu_backup_action: {
                //do the backup here
                final FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference bucket = storage.getReference();
                StorageReference notite = bucket.child(Constants.REMOTE_NOTE_FOLDER);
                uploadFiles(notite);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPermissionErrorText(){
        //set other views to invisible so that the error text view to be visible
        addButton.hide();
        notesList.setVisibility(View.INVISIBLE);
        errorTextView.setText(getResources().getString(R.string.permission_error_text));
    }

    public void uploadFiles(StorageReference parentFolder){
        final ArrayList<Integer> completedPositions = new ArrayList<Integer>();
        HashMap<File, Uri> filesPendingToBeUploaded = new HashMap<File, Uri>();
        ArrayList<Integer> states = PreferenceManager.getNotesStates(NotesList.this);
        if (parentFolder != null){
            final File[] files = FileManager.getFiles(NotesList.this);
            if (Build.VERSION.SDK_INT >= 23){
                String[] permissions = new String[]{Manifest.permission.ACCESS_NETWORK_STATE};
                if (ActivityCompat.checkSelfPermission(NotesList.this, Manifest.permission.ACCESS_NETWORK_STATE) != PermissionChecker.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(NotesList.this, permissions, INTERNET_PERMISSION_REQUEST_CODE);
                }
            }

            /*
                Get the number of files
             */
            int numFiles = 0;
            for (File f : files){
                numFiles++;
            }

            /*
                Show not connected message
             */
            ConnectivityManager manager = (ConnectivityManager)getSystemService(Service.CONNECTIVITY_SERVICE);
            if (manager != null){
                NetworkInfo info;
                if ((info = manager.getActiveNetworkInfo()) != null){
                    Log.d(TAG, "Connected to network with Info = " + info.getTypeName());
                } else {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NotesList.this);
                    Resources resources = getResources();
                    String title = resources.getString(R.string.no_internet_connection_title);
                    String message = resources.getString(R.string.no_internet_connection);
                    String ok_button = resources.getString(R.string.no_internet_connection_ok_button);
                    alertBuilder.setTitle(title);
                    alertBuilder.setMessage(message);
                    alertBuilder.setPositiveButton(ok_button, null);
                    alertBuilder.create().show();
                    return;
                }
            }

            /*
                Send the files
             */

            for (fileIndex = 0; fileIndex < files.length; ++fileIndex){
                final File f = files[fileIndex];
                try {
                    Log.d(TAG, "File " + f.getCanonicalPath() + " has size " + f.length());
                    /*
                        Check if connected to the internet
                     */

                    if (states != null){
                        if (states.get(fileIndex) == Constants.STATE_LOCAL){
                            StorageMetadata.Builder storageMetadataBuilder = new StorageMetadata.Builder();
                            storageMetadataBuilder.setContentType("text/plain");
                            StorageMetadata defaultMetadata = storageMetadataBuilder.build();
                            Uri.Builder builder = new Uri.Builder();
                            builder.scheme("file");
                            builder.encodedPath(f.getPath());
                            Log.d(TAG, "Uri = " + builder.build().toString());
                            StorageReference remoteFile = parentFolder.child(f.getName());
                            UploadTask uploadFileTask = remoteFile.putFile(builder.build(), defaultMetadata);
                            uploadFileTask.addOnFailureListener(new OnFailureListener(){
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    StorageException storageException = (StorageException)e;
                                    handleTaskException(storageException);
                                }
                            });
                            uploadFileTask.addOnSuccessListener(new OnSuccessListener(){
                                @Override
                                public void onSuccess(Object o) {
                                    UploadTask.TaskSnapshot taskSnapshot = (UploadTask.TaskSnapshot)o;
                                    StorageMetadata storageMetadata = null;
                                    if (taskSnapshot != null){
                                        storageMetadata = taskSnapshot.getMetadata();
                                    }
                                    if (storageMetadata != null) {
                                        String filename = storageMetadata.getName();
                                        Log.d(TAG, "Successfully uploaded " + filename);
                                        NoteManager.setNoteStateByName(NotesList.this, filename, Constants.STATE_GLOBAL);
                                        NoteManager.completeUploadByFilename(NotesList.this, filename);
                                        notesList.getAdapter().notifyDataSetChanged();
                                    }
                                }
                            });

                            uploadFileTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>(){
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    String filename = taskSnapshot.getMetadata().getName();
                                    Uri incompleteUri = taskSnapshot.getUploadSessionUri();
                                    if (NoteManager.getUploadUriByFilename(NotesList.this, filename) != null){
                                        //do nothing
                                    } else {
                                        NoteManager.setUriByFilename(NotesList.this, filename, incompleteUri);
                                    }
                                }
                            });
                        }
                    }

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void deleteCloudFile(final String filename) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference bucket = storage.getReference();
        StorageReference notesFolder = bucket.child(Constants.REMOTE_NOTE_FOLDER);
        final StorageReference fileToBeDeletedRefernce = notesFolder.child(filename);
        int index = FileManager.getFileIndexByName(NotesList.this, filename);
        Task deleteTask = fileToBeDeletedRefernce.delete();
        deleteTask.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                //do nothing
                Log.d(TAG, "Deleted " + filename + " from cloud");
            }
        });
        deleteTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StorageException exception = (StorageException) e;
                handleTaskException(exception);

            }
        });
    }

    public void handleTaskException(StorageException storageException){
        switch (storageException.getErrorCode()) {
            case StorageException.ERROR_BUCKET_NOT_FOUND: {
                Toast.makeText(NotesList.this, "Bucket not found", Toast.LENGTH_LONG).show();
                break;
            }
            case StorageException.ERROR_NOT_AUTHORIZED: {
                Toast.makeText(NotesList.this, "Not authorized to access the server folder", Toast.LENGTH_LONG).show();
                break;
            }
            case StorageException.ERROR_UNKNOWN: {
                Toast.makeText(NotesList.this, "An unknown error occured", Toast.LENGTH_LONG).show();
                break;
            }
            case StorageException.ERROR_OBJECT_NOT_FOUND: {
                Toast.makeText(NotesList.this, "Server folder does not exist", Toast.LENGTH_LONG).show();
                break;
            }
            case StorageException.ERROR_PROJECT_NOT_FOUND: {
                Toast.makeText(NotesList.this, "Remote server project not found", Toast.LENGTH_LONG).show();
                break;
            }
        }
    }


}
