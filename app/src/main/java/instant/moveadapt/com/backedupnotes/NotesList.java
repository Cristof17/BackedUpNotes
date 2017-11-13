package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import instant.moveadapt.com.backedupnotes.ActionMode.ActionModeMonitor;
import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.NotesContentProvider.NotesDatabaseContract;
import instant.moveadapt.com.backedupnotes.RecyclerView.NoteListRecyclerViewAdapter;

public class NotesList extends AppCompatActivity implements ActionMode.Callback{

    private static final int INTERNET_PERMISSION_REQUEST_CODE = 101;
    private static final int NEW_NOTE_RESULT_CODE = 2002;

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

    private int numFiles = 0;
    private int uploadedBytes = 0;
    private int totalUploadSize = 0;

    public volatile static boolean canGo = false;

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
//            FileManager.createNotesFolder(this);
        }

        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewNoteActivity.class);
                startActivityForResult(intent, NEW_NOTE_RESULT_CODE);
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
        Log.d(TAG, "onStart()");
        if (notesListRecyclerViewAdapter != null)
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, Thread.currentThread().getName());
        if (requestCode == NEW_NOTE_RESULT_CODE){
            if (resultCode == RESULT_OK){
                if (notesListRecyclerViewAdapter != null){
                    notesListRecyclerViewAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onActivityResult() notifyDataSetChanged()");
                }
            }
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()){
            case R.id.note_list_contextual_delete_action:
            {
                //delete the notes which have been selected
                ArrayList<Notita> notite = NoteManager.getNotesFromDatabase(this);
                for (int i = 0; i < notite.size() ; ++i) {
                    if (ActionModeMonitor.getActivated(getApplicationContext(), i)) {
                        ActionModeMonitor.setActivated(i, false);
                        ContentResolver resolver = getContentResolver();
                        String whereClause = NotesDatabaseContract.Notite._ID + " = ? ";
                        String[] whereArgs = {notite.get(i).getId() +""};
                        Log.d(TAG, "Deleting note with id = " + notite.get(i).getId());
                        int deletedRows = resolver.delete(NotesDatabaseContract.Notite.URI,
                                whereClause,
                                whereArgs);
                        Log.d(TAG, "Deleted " + deletedRows + " rows ");
                        //delete from cloud
                        notesListRecyclerViewAdapter.notifyItemRemoved(i);
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
//                deleteWhatNeedsToBeDeletedFromCloud();
//                uploadFiles(notite);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if (notesListRecyclerViewAdapter != null)
            notesListRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void showPermissionErrorText(){
        //set other views to invisible so that the error text view to be visible
        addButton.hide();
        notesList.setVisibility(View.INVISIBLE);
        errorTextView.setText(getResources().getString(R.string.permission_error_text));
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
