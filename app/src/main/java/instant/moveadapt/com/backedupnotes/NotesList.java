package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import instant.moveadapt.com.backedupnotes.Managers.FileManager;
import instant.moveadapt.com.backedupnotes.Managers.PreferenceManager;
import instant.moveadapt.com.backedupnotes.RecyclerView.NewNoteActivity;
import instant.moveadapt.com.backedupnotes.RecyclerView.NoteListRecyclerViewAdapter;

public class NotesList extends AppCompatActivity {

    private static final int NEW_NOTE_REQUEST_CODE = 100;

    private FloatingActionButton addButton;
    private Button backupButton;
    private RecyclerView notesList;
    private NoteListRecyclerViewAdapter notesListRecyclerViewAdapter;
    private TextView errorTextView;//used for when t
    // he permission is not granted to show to the user
    //she cannot use the app
    private static final String TAG = "[NOTE_LIST]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
//        FileManager.deleteAllFiles(this);
//        int files = FileManager.getNumNotes(this);
//        Toast.makeText(this, "Number of notes = " + files, Toast.LENGTH_SHORT).show();
        /*
            Bind Views
         */
        addButton = (FloatingActionButton)findViewById(R.id.add_note_button);
        backupButton = (Button)findViewById(R.id.backup_button);
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
                startActivityForResult(intent, NEW_NOTE_REQUEST_CODE);
            }
        });

        notesListRecyclerViewAdapter = new NoteListRecyclerViewAdapter(this, notesList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        notesList.setAdapter(notesListRecyclerViewAdapter);
        notesList.setLayoutManager(llm);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_NOTE_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                PreferenceManager.addState(NotesList.this, Constants.STATE_LOCAL);
            } else if (resultCode == RESULT_CANCELED){
                //do nothing
            }
        }
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
                if (errorTextView !=  null && addButton != null && backupButton != null && notesList != null){
                    showPermissionErrorText();
                } else {
                    finish();
                }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.note_list_menu_backup_action:
            {
                //do the backup here
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPermissionErrorText(){
        //set other views to invisible so that the error text view to be visible
        addButton.hide();
        backupButton.setVisibility(View.INVISIBLE);
        notesList.setVisibility(View.INVISIBLE);
        errorTextView.setText(getResources().getString(R.string.permission_error_text));
    }
}
