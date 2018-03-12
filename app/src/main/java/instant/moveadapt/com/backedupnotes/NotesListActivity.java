package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.firebase.storage.StorageMetadata;

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
     * Execution comes here when the app is started and the activity created
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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

        onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.i(TAG, "Verification completed");
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
                appBarLayout.setVisibility(View.VISIBLE);

                notesToDelete.clear();
                notesAdapter.notifyDataSetChanged();
            }
        };

        mAuth = FirebaseAuth.getInstance();
    }

    /*
     * Execution comes here when the app is already starremoveted and the activity
     * already exists, but needs to go into foreground because it was coverd
     * by another activity that just finished
     */
    @Override
    protected void onRestart() {
        super.onRestart();                /*
                 * Delete selected notes
                 */
                for (Note note : notesToDelete){
                    String whereClause = NotesDatabase.DatabaseContract._ID + " = ? ";
                    String whereArgs[] = new String[] {note.id.toString()};
                    getContentResolver().delete(NotesDatabase.DatabaseContract.URI,
                            whereClause,
                            whereArgs);
                }

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
}
