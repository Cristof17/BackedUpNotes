package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.logging.LogManager;

import javax.crypto.SecretKey;
import instant.moveadapt.com.backedupnotes.Cloud.CloudManager;
import instant.moveadapt.com.backedupnotes.Cloud.NoteUploadedCallback;
import instant.moveadapt.com.backedupnotes.Cloud.StartUploadCallback;
import instant.moveadapt.com.backedupnotes.Database.DatabaseManager;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Encrypt.CryptStartCallback;
import instant.moveadapt.com.backedupnotes.Encrypt.CryptUpdateCallback;
import instant.moveadapt.com.backedupnotes.Encrypt.EncryptManager;
import instant.moveadapt.com.backedupnotes.Pojo.Note;
import instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager;
import instant.moveadapt.com.backedupnotes.RecyclerView.NoteListRecyclerViewAdapter;
import instant.moveadapt.com.backedupnotes.RecyclerView.SelectedRecyclerViewItemCallback;

public class NotesListActivity extends AppCompatActivity implements SelectedRecyclerViewItemCallback,
        StartUploadCallback, NoteUploadedCallback, CryptStartCallback, CryptUpdateCallback{

    /*
     * Permission requests code
     */
    private static final int INTERNET_PERMISSION_REQUEST_CODE = 101;
    public static final int READ_WRITE_PERMISSION_REQ_CODE = 102;

    private static final String TAG = "NOTES_LIST";
    private RecyclerView notesRecyclerView;
    private NoteListRecyclerViewAdapter notesAdapter;
    private TextView messageTextView;
    private LinearLayout popUpLinearLayout;
    private ProgressBar popUpProgressBar;
    private ImageButton settingsButton;

    private ImageButton exitButton;
    private FloatingActionButton actionButton;

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
    private CoordinatorLayout rootLayout;
    private int countNotes;
    private int maxNotes;
    private AppBarLayout toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_notes_list);

        if (PreferenceManager.areEncrypted(getApplicationContext())){
            Intent decryptIntent = new Intent(getApplicationContext(), Crypt.class);
            decryptIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(decryptIntent);
        }

        actionButton = (FloatingActionButton) findViewById(R.id.notes_list_activity_action_btn);
        toolbar = (AppBarLayout)findViewById(R.id.notes_list_activity_toolbar);
        messageTextView = (TextView) findViewById(R.id.error_text_view);
        notesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        exitButton = (ImageButton) findViewById(R.id.notes_list_activity_exit_btn);
        rootLayout = (CoordinatorLayout)findViewById(R.id.notes_list_coordinator_layout);
        notesAdapter = new NoteListRecyclerViewAdapter(NotesListActivity.this, this);
        popUpLinearLayout = (LinearLayout)findViewById(R.id.activity_notes_list_popup_ll);
        popUpProgressBar = (ProgressBar) findViewById(R.id.activity_notes_list_popup_pb);
        settingsButton = (ImageButton) findViewById(R.id.notes_list_activity_settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        popUpLinearLayout.post(new Runnable() {
            @Override
            public void run() {
                popUpLinearLayout.setTranslationY(-popUpLinearLayout.getMeasuredHeight());
            }
        });

        /*
         *  Request permission for android 6.0 and upwards
         */
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            /*
             * Se blocheaza ca sa cer permisiuni
             */
            ActivityCompat.requestPermissions(NotesListActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION_REQ_CODE);
        }

        exitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                exit();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                if (EncryptManager.notesAreEncrypted(getApplicationContext())){
                    //show cloud icon
                    if (!CloudManager.isLoggedIn()) {
                        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(loginIntent);
                    }else{
                        CloudManager.deleteNotesToBeDeletedFromCloud(getApplicationContext());
                        CloudManager.updateCloudNotes(getApplicationContext(), NotesListActivity.this, NotesListActivity.this);
                    }

                } else if (EncryptManager.notesAreCorrectlyDecrypted(getApplicationContext())){
                    //show add icon
                    Intent startNewNoteIntent = new Intent(getApplicationContext(), NewNoteActivity.class);
                    startActivity(startNewNoteIntent);
                } else{
                    //show checkbutton
                    createReverseDecryptionDialog().show();
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
//                appBarLayout.setVisibility(View.GONE);
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
                            getContentResolver().registerContentObserver(NotesDatabase.DatabaseContract.URI, true,
                                    new ContentObserver(new Handler()) {
                                        @Override
                                        public void onChange(boolean selfChange) {
                                            super.onChange(selfChange);
                                        }

                                        @Override
                                        public void onChange(boolean selfChange, Uri uri) {
                                            super.onChange(selfChange, uri);
                                            Log.d(TAG, "onChange(): " + uri.toString());
                                        }
                                    });
                            CloudManager.deleteNoteFromCloud(getApplicationContext(), note);
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
                Log.d(TAG, "onDestroyActionMode()");
                actionMode = null;
                notesAdapter.resetCursor();
//                appBarLayout.setVisibility(View.VISIBLE);

                notesToDelete.clear();
                notesAdapter.resetCursor();
                notesAdapter.notifyDataSetChanged();
                if (notesAdapter.getItemCount() == 0){
                    instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.setEncrypted(
                            getApplicationContext(),
                            false);
                    updateUIAccordingToEncryptionStatus();
                    exitButton.setEnabled(false);
                    toolbar.setVisibility(View.INVISIBLE);
                }else{
                    toolbar.setVisibility(View.VISIBLE);
                }
            }
        };

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

        CloudManager.deleteNotesToBeDeletedFromCloud(getApplicationContext());
        updateUIAccordingToEncryptionStatus();
        if (DatabaseManager.getNotesCount(this) == 0){
            toolbar.setVisibility(View.INVISIBLE);
        }else{
            toolbar.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG, "onPostCreate()");
    }

    private AlertDialog createReverseDecryptionDialog() {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(NotesListActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.reverse_decryption_alert_dialog_layout, null, false);
        alertBuilder.setView(rootView);
        alertBuilder.setTitle("Do notes look like before?");
//        final TextView warningTextView = (TextView) rootView.findViewById(R.id.cloud_credentials_bottom_sheet_password);
        alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                looksGoodSelected();
                updateUIAccordingToEncryptionStatus();
            }
        });
        alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reverseDecryption();
            }
        });

        return alertBuilder.create();
    }

    private void looksGoodSelected() {
        PreferenceManager.clearCachedPassword(getApplicationContext());
    }

    private String getCachedPassword(){
        return instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.getLooksGoodPassword(
                getApplicationContext());

    }

    private void reverseDecryption() {
        SecretKey key = EncryptManager.getKey(getApplicationContext());
        EncryptManager.encryptAllNotes(getApplicationContext(), getCachedPassword(), key, null, null);
        EncryptManager.setEncrypted(getApplicationContext(), true);
        PreferenceManager.clearCachedPassword(getApplicationContext());

        Intent intent = new Intent(getApplicationContext(), Crypt.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("CAME_FROM_NOTES_LIST", true);
        startActivity(intent);
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
        updateUIAccordingToEncryptionStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermission()");
        if (requestCode == READ_WRITE_PERMISSION_REQ_CODE){
            int result = grantResults[0];

            if (result == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Permission for rd/wr to external storage is granted");
            } else {
                Log.d(TAG, "Permission for rd/wr to external storag e is denied");
                if (messageTextView !=  null && actionButton != null && notesRecyclerView != null){
                    actionButton.setVisibility(View.INVISIBLE);
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
                Log.d(TAG, "onConfigurationChanged():Landscape");
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.d(TAG, "onConfigurationChanged():Portrait");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    private void updateUIAccordingToEncryptionStatus(){

        if (EncryptManager.notesAreCorrectlyDecrypted(getApplicationContext())) {
            actionButton.setImageResource(R.drawable.ic_add_white);
            actionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
            actionButton.setAlpha(1.0f);
            exitButton.setImageResource(R.drawable.ic_baseline_vpn_key);
            exitButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
            exitButton.setEnabled(true);
//            settingsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
            settingsButton.setImageResource(R.drawable.ic_baseline_settings);
        }else if (!EncryptManager.notesAreCorrectlyDecrypted(getApplicationContext())){
            actionButton.setImageResource(R.drawable.ic_check_white);
            actionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.warning)));
            actionButton.setAlpha(1.0f);
            exitButton.setEnabled(false);
            exitButton.setImageResource(R.drawable.ic_baseline_vpn_key_faded);
//            exitButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
//            settingsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
            settingsButton.setImageResource(R.drawable.ic_baseline_settings);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSavedInstanceState()");
        Gson gson = new Gson();
        String notesToDeleteJson = gson.toJson(notesToDelete);
        outState.putString("notesToDelete", notesToDeleteJson);
        outState.putBoolean("actionMode", actionMode != null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        if(DatabaseManager.getNotesCount(getApplicationContext()) == 0){
            exitButton.setEnabled(false);
            toolbar.setVisibility(View.INVISIBLE);
        } else {
            toolbar.setVisibility(View.VISIBLE);
            if (EncryptManager.notesAreCorrectlyDecrypted(getApplicationContext()))
                exitButton.setEnabled(true);
            else
                exitButton.setEnabled(false);
        }
    }

    @Override
    public void onNoteUploaded(Note note) {
        Log.d(TAG, "onNoteUploaded()");
        popUpProgressBar.setProgress(popUpProgressBar.getProgress() + 1);
        if (popUpProgressBar.getProgress() == popUpProgressBar.getMax())
            reverseAnimateViews();

    }

    @Override
    public void onStartUpload(int count) {
        Log.d(TAG, "onStartUpload()");
        popUpProgressBar.setMax(count);
        if (popUpLinearLayout.getMeasuredHeight() > 0) {
            animateViews();
        }
    }

    private void animateViews() {
        ObjectAnimator recyclerViewAnimtor = ObjectAnimator.ofFloat(notesRecyclerView,
                "translationY",
                0, popUpLinearLayout.getMeasuredHeight());
        recyclerViewAnimtor.setDuration(300);
        recyclerViewAnimtor.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator popUpAnimator = ObjectAnimator.ofFloat(popUpLinearLayout,
                "translationY",
                -popUpLinearLayout.getMeasuredHeight(), 0);
        popUpAnimator.setDuration(300);
        popUpAnimator.setInterpolator(new AccelerateInterpolator());

        recyclerViewAnimtor.start();
        popUpAnimator.start();
    }

    public void reverseAnimateViews(){
        ObjectAnimator recyclerViewAnimtor = ObjectAnimator.ofFloat(notesRecyclerView,
                "translationY",
                popUpLinearLayout.getMeasuredHeight(), 0);
        recyclerViewAnimtor.setDuration(300);
        recyclerViewAnimtor.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator popUpAnimator = ObjectAnimator.ofFloat(popUpLinearLayout,
                "translationY",
                0, -popUpLinearLayout.getMeasuredHeight() );
        popUpAnimator.setDuration(300);
        popUpAnimator.setInterpolator(new AccelerateInterpolator());

        recyclerViewAnimtor.start();
        popUpAnimator.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed()");
        if (PreferenceManager.arePartiallyDecrypted(getApplicationContext())){
            PreferenceManager.setExitWithoutEncrypt(getApplicationContext(), true);
        }

        if (!PreferenceManager.areEncrypted(getApplicationContext())){
            PreferenceManager.setExitWithoutEncrypt(getApplicationContext(), true);
        }
        finish();
    }

    private void exit(){
        if (!PreferenceManager.arePartiallyDecrypted(getApplicationContext()) && DatabaseManager.getNotesCount(getApplicationContext()) != 0) {
            Intent intent = new Intent(getApplicationContext(), Crypt.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("CAME_FROM_NOTES_LIST", true);
            startActivity(intent);
        } else {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void cryptographyOperationStarted(boolean isEncryption) {
        Log.d(TAG, "cryptographyOperationStarted()");
        maxNotes = DatabaseManager.getNotesCount(getApplicationContext());
        countNotes = 0;
    }

    @Override
    public void cryptUpdate(boolean isEncryption) {
        countNotes ++;
        Log.d(TAG, "cryptUpdate()");
        if (countNotes == maxNotes){
            finish();
        }
    }

    public CoordinatorLayout getRootLayout(){
        return rootLayout;
    }
}
