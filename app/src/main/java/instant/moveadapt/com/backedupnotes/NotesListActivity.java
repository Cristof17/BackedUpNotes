package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import instant.moveadapt.com.backedupnotes.Database.NotesContentProvider;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Pojo.Note;
import instant.moveadapt.com.backedupnotes.RecyclerView.NoteListRecyclerViewAdapter;
import instant.moveadapt.com.backedupnotes.RecyclerView.SelectedRecyclerViewItemCallback;

public class NotesListActivity extends AppCompatActivity implements SelectedRecyclerViewItemCallback{

    /*
     * Permission requests code
     */
    private static final int INTERNET_PERMISSION_REQUEST_CODE = 101;
    public static final int READ_WRITE_PERMISSION_REQ_CODE = 102;

    private static final String TAG = "[NOTE_LIST]";

    private FloatingActionButton addButton;
    private RecyclerView notesRecyclerView;
    private NoteListRecyclerViewAdapter notesAdapter;

    private TextView messageTextView;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    private Button loginButton;
    private Button codeButton;
    private Button uploadButton;


    /*
     * In the adapter the rootViews for each list item
     * has a click listener attached which if the
     * action mode is active marks a note
     * to be deleted from database
     *
     * The action of deletion is implemented in this class
     * and the addDeviceForDeletion and removeDeviceForDeletion
     * methods fire from the adapter and suggest which Notes
     * ought to be deleted when the actionMode delete button is
     * pressed
     *
     * notesToDelete is the set of notes marked for deletion
     */
    private Set<Note> notesToDelete;
    /*
     * The actionMode object which controls when the actionMode finishes
     * by calling its' finish() method
     *
     */
    public ActionMode actionMode;
    public ActionMode.Callback actionModeCallback;

    /*
     * Firebase auth object (this is the object used to authenticate using
     * credential object)
     */
    FirebaseAuth mAuth;

    /*
     * Firebase authentication callbacks
     */
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationStateChangedCallbacks;

    /*
     * Verification ID for the phone request returned in onCodeSent
     */
    private String verificationID;

    /*
     * Resend token (don't know what this is used for yet
     */
    private PhoneAuthProvider.ForceResendingToken resendToken;

    /*
     * Firebase updatedValue listener
     */
    private ValueEventListener databaseValueListener;

    /*
     * Execution comes here when the app is started and the activity created
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        //TODO
//        addData();

        setContentView(R.layout.activity_notes_list);

        addButton = (FloatingActionButton) findViewById(R.id.add_note_button);
        messageTextView = (TextView) findViewById(R.id.error_text_view);
        notesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        appBarLayout = (AppBarLayout) findViewById(R.id.activity_notes_list_appbarlayout);
        toolbar = (Toolbar) appBarLayout.findViewById(R.id.activity_notes_list_toolbar);
        loginButton = (Button) findViewById(R.id.notes_list_activity_login_btn);
        codeButton = (Button) findViewById(R.id.notes_list_activity_code_btn);
        uploadButton = (Button) findViewById(R.id.notes_list_activity_upload_btn);

        notesAdapter = new NoteListRecyclerViewAdapter(NotesListActivity.this, this);

        /*
         *  Request permission for android 6.0 and upwards
         */
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            /*
             * Se blocheaza ca sa cer permisiuni
             */
            ActivityCompat.requestPermissions(NotesListActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION_REQ_CODE);
        }

        setSupportActionBar(toolbar);

        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent startNewNoteIntent = new Intent(getApplicationContext(), NewNoteActivity.class);
                startActivity(startNewNoteIntent);
            }
        });

        codeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                final EditText codeEditText = new EditText(NotesListActivity.this);
                builder.setView(codeEditText);
                builder.setTitle("Enter verification code");
                builder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String code = codeEditText.getText().toString();

                        if (mAuth != null){
                            PhoneAuthCredential phoneCredential = PhoneAuthProvider.getCredential(verificationID, code);
                            signIn(phoneCredential);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create().show();

            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference notesDb = db.getReference();
                saveNotesToCloud(notesDb);
            }
        });

        onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.i(TAG, "Verification completed " + phoneAuthCredential.getProvider());
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.e(TAG, "Verification failed " + e.getMessage());
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Log.d(TAG, "Code sent");
                verificationID = s;
                resendToken = forceResendingToken;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final EditText phoneNumberEditText= new EditText(NotesListActivity.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                builder.setTitle("Phone number");
                builder.setView(phoneNumberEditText);
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumberEditText.getText().toString(),
                                120,
                                TimeUnit.SECONDS,
                                NotesListActivity.this,
                                onVerificationStateChangedCallbacks);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
            }
        });

        notesRecyclerView.setAdapter(notesAdapter);

        /*
         * from Android documentation
         */
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,false));

        /*
         * Init actionModeCallback
         */
        actionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = new MenuInflater(NotesListActivity.this);
                menuInflater.inflate(R.menu.note_list_contextual_menu, menu);
                appBarLayout.setVisibility(View.GONE);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                Log.d("notes.db", "Action Mode is ");
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch(item.getItemId()){
                    case R.id.note_list_contextual_delete_action:
                        /*
                         * Delete selected notes
                         */
                        for (Note note : notesToDelete){
                            String whereClause = NotesDatabase.DatabaseContract._ID + " = ? ";
                            String whereArgs[] = new String[] {note.id.toString()};
                            getContentResolver().delete(NotesDatabase.DatabaseContract.URI,
                                    whereClause,
                                    whereArgs);
                            deleteNoteFromCloud(note);

                        }
                        actionMode.finish();
                        return true;
                    default:
                        //do nothing
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                notesAdapter.resetCursor();
                appBarLayout.setVisibility(View.VISIBLE);

                notesToDelete.clear();
                notesAdapter.notifyDataSetChanged();
            }
        };

        mAuth = FirebaseAuth.getInstance();

        //TODO Remove later
//        loginButton.setVisibility(View.INVISIBLE);
//        codeButton.setVisibility(View.INVISIBLE);

        /*
         * Restore the state
         */
        if (savedInstanceState != null){

            Gson gson = new Gson();
            String notesToDeleteJson = (String) savedInstanceState.get("notesToDelete");
            if (notesToDeleteJson != null && !notesToDeleteJson.equals("")){

                Type collectionType = new TypeToken<Set<Note>>() {}.getType();
                notesToDelete = gson.fromJson(notesToDeleteJson, collectionType);
            }else{
                Log.e(TAG, "Cannot restore notesToDelete, string in bundle is null");
            }

            boolean isActionModeActive = (boolean) savedInstanceState.get("actionMode");

            if (isActionModeActive){
                actionMode = startActionMode(actionModeCallback);
            }else{
                Log.e(TAG, "Action Mode is null");
            }

            notesAdapter.resetCursor();
            notesAdapter.notifyDataSetChanged();
        }

        databaseValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error : " + databaseError.getMessage());
            }
        };
    }

    /*
     * Execution comes here when the app is already starremoveted and the activity
     * already exists, but needs to go into foreground because it was coverd
     * by another activity that just finished
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");

        notesAdapter.resetCursor();
        notesAdapter.notifyDataSetChanged();
    }

    //TODO Remove
    void addData(){
        for (int i = 0; i < 30; ++i) {
            ContentValues vals = new ContentValues();
            vals.put(NotesDatabase.DatabaseContract._ID, UUID.randomUUID().toString());
            vals.put(NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP, System.currentTimeMillis());
            vals.put(NotesDatabase.DatabaseContract.COLUMN_TEXT, "Note " + i);
            getContentResolver().insert(NotesDatabase.DatabaseContract.URI, vals);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_WRITE_PERMISSION_REQ_CODE){
            int result = grantResults[0];

            if (result == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Permission for rd/wr to external storage is granted");
            } else {
                Log.d(TAG, "Permission for rd/wr to external storag e is denied");
                if (messageTextView !=  null && addButton != null && notesRecyclerView != null){
                    addButton.setVisibility(View.INVISIBLE);
                    notesRecyclerView.setVisibility(View.INVISIBLE);
                    messageTextView.setVisibility(View.VISIBLE);
                    /*
                     * Show custom text for error
                     */
                    messageTextView.setText(getResources().getString(R.string.permission_error_text));
                } else {
                    /*
                     * Should not get here
                     */
                    finish();
                }
            }
        }

        /*
         * Revise the action when the user grants or denies permission for internet
         */
        if (requestCode == INTERNET_PERMISSION_REQUEST_CODE){
            int result = grantResults[0];

            if (result == PackageManager.PERMISSION_GRANTED){
                Resources res = getResources();
                String permissionGrantedMessage = res.getString(R.string.permission_granted_message);
                Toast.makeText(NotesListActivity.this, permissionGrantedMessage, Toast.LENGTH_LONG).show();
            } else {
                Resources resources = getResources();
                String noPermissionWarning = resources.getString(R.string.cannot_upload_warning);
                Toast.makeText(NotesListActivity.this, noPermissionWarning, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation){
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.d(TAG, "Landscape");
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.d(TAG, "Portrait");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void addNoteForDeletion(Note note) {
        if (notesToDelete == null){
            notesToDelete = new ArraySet<>();
        }
        notesToDelete.add(note);
    }

    @Override
    public void removeNoteFromDeletion(Note note) {
        notesToDelete.remove(note);
        if (notesToDelete.size() == 0){
            actionMode.finish();
        }
    }

    public boolean isSelected(Note note){

        if (notesToDelete == null){
            return false; //there is no note marked
        }
        boolean contains = notesToDelete.contains(note);
        return notesToDelete.contains(note);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSavedInstanceState()");
        Gson gson = new Gson();
        String notesToDeleteJson = gson.toJson(notesToDelete);
        outState.putString("notesToDelete", notesToDeleteJson);
        outState.putBoolean("actionMode", actionMode != null);
    }

    public void saveNotesToCloud(DatabaseReference db){
        Cursor c = getContentResolver().query(NotesDatabase.DatabaseContract.URI,
                NotesDatabase.DatabaseContract.getTableColumns(),
                null,
                null,
                null);

        if (c != null){
            do {
                c.moveToNext();
                Note currNote = convertToNote(c);
                DatabaseReference ref = db.child("+40721858913").child(currNote.id.toString());
                ref.setValue(currNote);
            }while(!c.isLast());
            Log.d(TAG, "Uploaded notes");
        }else{
            Toast.makeText(getApplicationContext(), "No notes to save ", Toast.LENGTH_LONG).show();
        }
    }

    public void deleteNoteFromCloud( Note note){

        FirebaseDatabase firebaseDb = FirebaseDatabase.getInstance();
        DatabaseReference db = firebaseDb.getReference();
        db.addValueEventListener(databaseValueListener);

        DatabaseReference userRef = db.child("+40721858913");
        userRef.child(note.id.toString()).removeValue();
    }

    private void signIn(PhoneAuthCredential phoneCredential){
        mAuth.signInWithCredential(phoneCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    Log.d(TAG, "Logged in using " +  user.getPhoneNumber());
                }else{
                    Log.e(TAG, "Login using phone number failed");
                }
            }
        });
    }

    private Note convertToNote(Cursor c){
        if (c == null){
            return null;
        }
        Note n = null;

        String text = c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract.COLUMN_TEXT));
        long timestamp = Long.parseLong(c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract.COLUMN_TIMESTAMP)));
        UUID id = UUID.fromString(c.getString(c.getColumnIndex(NotesDatabase.DatabaseContract._ID)));
        n = new Note(id, text, timestamp);

        return n;
    }
}
