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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.util.ArraySet;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
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
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import instant.moveadapt.com.backedupnotes.Cloud.BottomSheetCallback;
import instant.moveadapt.com.backedupnotes.Cloud.CloudCredentialsBottomSheet;
import instant.moveadapt.com.backedupnotes.Cloud.CloudOptionsBottomSheet;
import instant.moveadapt.com.backedupnotes.Database.NotesContentProvider;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Pojo.Note;
import instant.moveadapt.com.backedupnotes.RecyclerView.NoteListRecyclerViewAdapter;
import instant.moveadapt.com.backedupnotes.RecyclerView.SelectedRecyclerViewItemCallback;

public class NotesListActivity extends AppCompatActivity implements SelectedRecyclerViewItemCallback,
        BottomSheetCallback{

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
    private Button uploadButton;
    private Button logoutButton;
    private Button encryptButton;

    /*
     * UI For authentication
     */
    private static String phoneNumber;
    private AlertDialog authenticateDialog;


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
     * Firebase updatedValue listener
     */
    private ValueEventListener databaseValueListener;

    /*
     * When user clicks login or register, an alert dialog appears
     * with the credentials fields for the user to enter email and password
     * and a button (Login/Register) which calls the onLoginSelected and
     * onRegisterSelected
     */
    private AlertDialog credentialsDialog;

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
        uploadButton = (Button) findViewById(R.id.notes_list_activity_upload_btn);
        logoutButton = (Button) findViewById(R.id.notes_list_activity_logout_btn);
        encryptButton = (Button) findViewById(R.id.notes_list_activity_encrypt_btn);

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

        uploadButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference notesDb = db.getReference();
                saveNotesToCloud(notesDb);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

//                showPhoneNumberDialog();
                CloudOptionsBottomSheet btmSheet = new CloudOptionsBottomSheet(NotesListActivity.this);
                btmSheet.setBottomSheetCallback(NotesListActivity.this);
                btmSheet.show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth != null && mAuth.getCurrentUser() != null){
                    mAuth.signOut();
                    Toast.makeText(getApplicationContext(), "Log out successful ", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Not logged in ", Toast.LENGTH_LONG).show();
                }
            }
        });

        encryptButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String password = "pass";
                PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
                try {
                    SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
                    SecretKey keyTmp = factory.generateSecret(keySpec);
                    SecretKey secret = new SecretKeySpec(keyTmp.getEncoded(), "AES");
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, secret);
                    String message = "Mesaj";
                    byte[] encryptedMessageBytes = cipher.doFinal(message.getBytes());
                    String encryptedMessage = new String(encryptedMessageBytes);
                    Log.d(TAG, "encrypted message = " + encryptedMessage);

                    cipher.init(Cipher.DECRYPT_MODE, secret);
                    byte[] decryptedMessageBytes = cipher.doFinal(encryptedMessageBytes);
                    String decryptedMessage = new String(decryptedMessageBytes);
                    Log.d(TAG, "decrypted message = " + decryptedMessage);

                }catch (NoSuchAlgorithmException e){
                    Log.e(TAG, "No such algorithm");
                } catch (InvalidKeySpecException e) {
                    Log.e(TAG, "Invalid key spec");
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    Log.e(TAG, "No such padding");
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    Log.e(TAG, "Invalid key ");
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    Log.e(TAG, "bad padding ");
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    Log.e(TAG, "illegal block size" );

                    e.printStackTrace();
                }
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

        deleteNotesToBeDeletedFromCloud();
    }

    private void deleteNotesToBeDeletedFromCloud() {

        if (mAuth.getCurrentUser() != null){
            Log.d(TAG, "Deleting pending notes from the cloud");
            Cursor c = getContentResolver().query(NotesDatabase.DeleteNotesContract.URI,
                    NotesDatabase.DeleteNotesContract.getTableColumns(),
                    null,
                    null,
                    null);

            if (c != null && c.getCount() > 0){
                do {
                    c.moveToNext();
                    Note currNote = convertToNote(c);
                    deleteNoteFromCloud(currNote);

                    /**
                     * Delete the note from To be deleted from cloud Table in the db
                     */
                    String whereClause = NotesDatabase.DeleteNotesContract._ID + " = ? ";
                    String selectionArgs[] = {currNote.id.toString()};
                    getContentResolver().delete(NotesDatabase.DeleteNotesContract.URI,
                            whereClause,
                            selectionArgs);
                }while (!c.isLast());
            }
        }
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

        if (mAuth != null && mAuth.getCurrentUser() != null) {
            Cursor c = getContentResolver().query(NotesDatabase.DatabaseContract.URI,
                    NotesDatabase.DatabaseContract.getTableColumns(),
                    null,
                    null,
                    null);

            if (c != null) {
                do {

                    c.moveToNext();
                    Note currNote = convertToNote(c);
                    String childName = mAuth.getCurrentUser().getUid();
                    DatabaseReference ref = db.child(childName).child(currNote.id.toString());
                    ref.setValue(currNote);

                } while (!c.isLast());
                Log.d(TAG, "Uploaded notes");
            } else {
                Toast.makeText(getApplicationContext(), "No notes to save ", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please login to save notes to cloud", Toast.LENGTH_LONG).show();
        }
    }

    public void deleteNoteFromCloud(Note note){

        if (mAuth != null && mAuth.getCurrentUser() != null) {
            FirebaseDatabase firebaseDb = FirebaseDatabase.getInstance();
            DatabaseReference db = firebaseDb.getReference();
            db.addValueEventListener(databaseValueListener);

            String childName = mAuth.getCurrentUser().getUid();
            DatabaseReference userRef = db.child(childName);
            userRef.child(note.id.toString()).removeValue();
        }else{
            /*
             * Save the note to be deleted later
             */
            storeNoteForLaterDelete(note);
        }
    }

    public void storeNoteForLaterDelete(Note note){
        ContentValues vals = new ContentValues();
        vals.put(NotesDatabase.DeleteNotesContract._ID, note.id.toString());
        vals.put(NotesDatabase.DeleteNotesContract.COLUMN_TEXT, note.getText());
        vals.put(NotesDatabase.DeleteNotesContract.COLUMN_TIMESTAMP, note.getTimestamp());
        Uri uri = getContentResolver().insert(NotesDatabase.DeleteNotesContract.URI, vals);
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

    @Override
    public void onLoginSelected(String email, String password) {
        Log.d(TAG, "Login selected");

        if (credentialsDialog != null){
            credentialsDialog.dismiss();
        }else{
            Log.e(TAG, "Alert dialog should be visible");
        }

        if (mAuth != null){
            try {

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Logged in as " + mAuth.getCurrentUser().getEmail());
                                deleteNotesToBeDeletedFromCloud();
                            } else {
                                Toast.makeText(getApplicationContext(), "Cannot login" + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                                Log.d(TAG, task.getException().toString());
                                Log.d(TAG, task.getResult().toString());
                            }
                        }catch (Exception e){
                            if (e instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(getApplicationContext(), "Invalid password", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Login failed " + e.getMessage());
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRegisterSelected(String email, String password) {
        Log.d(TAG, "Register selected");

        if (credentialsDialog != null){
            credentialsDialog.dismiss();
        }else{
            Log.e(TAG, "Alert dialog should be visible");
        }


        if (mAuth != null){
            try {

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        try {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Registered in as " + mAuth.getCurrentUser().getEmail());
                            } else {
                                Log.d(TAG, "Registration failure " + task.getException().toString());
                                Log.d(TAG, "Registration failure " + task.getResult().toString());
                            }
                        }catch (Exception e){
                            if (e instanceof FirebaseAuthWeakPasswordException){
                                Toast.makeText(getApplicationContext(), "Password too weak", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Register failed " + e.getMessage());
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onOneOfTheOptionsSelected(String optionText) {
        credentialsDialog = createCredentialsDialog(optionText);
        credentialsDialog.show();
    }

    private AlertDialog createCredentialsDialog(final String buttonText){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NotesListActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.cloud_credentials_bottom_sheet, null, false);
        alertBuilder.setView(rootView);
        final EditText usernameText = (EditText) rootView.findViewById(R.id.cloud_credentials_bottom_sheet_username);
        final EditText passwordText = (EditText) rootView.findViewById(R.id.cloud_credentials_bottom_sheet_password);
        Button actionButton = (Button) rootView.findViewById(R.id.cloud_credentials_bottom_sheet_btn);

        actionButton.setText(buttonText);
        actionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (buttonText.toLowerCase().equals("login")) {
                    if (((BottomSheetCallback)NotesListActivity.this) != null) {
                        ((BottomSheetCallback)NotesListActivity.this).onLoginSelected(usernameText.getText().toString(), passwordText.getText().toString());
                    }
                } else if (buttonText.toLowerCase().equals("register")){
                    if (((BottomSheetCallback)NotesListActivity.this) != null) {
                        ((BottomSheetCallback)NotesListActivity.this).onRegisterSelected(usernameText.getText().toString(), passwordText.getText().toString());
                    }
                }
            }
        });

        return alertBuilder.create();
    }
}
