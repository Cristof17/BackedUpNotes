package instant.moveadapt.com.backedupnotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import instant.moveadapt.com.backedupnotes.Cloud.CloudManager;
import instant.moveadapt.com.backedupnotes.Database.DatabaseManager;
import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Encrypt.CryptStartCallback;
import instant.moveadapt.com.backedupnotes.Encrypt.CryptUpdateCallback;
import instant.moveadapt.com.backedupnotes.Encrypt.EncryptManager;
import instant.moveadapt.com.backedupnotes.Pojo.Note;
import instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager;

import javax.crypto.SecretKey;

/**
 * Created by cristof on 11.05.2018.
 */

public class Crypt extends AppCompatActivity implements View.OnClickListener, CryptStartCallback,
        CryptUpdateCallback{

    private Button btn0;
    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;
    private Button btn6;
    private Button btn7;
    private Button btn8;
    private Button btn9;
    private ImageButton delBtn;
    private ImageButton doneBtn;
    private TextView text;
    private Toolbar toolbar;
    private static final String TAG = "CRYPT";
    private int countNotes;
    private int maxNotes;
    private TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startIntent = null;

        setContentView(R.layout.crypt_activity_note);
        btn1 = (Button)findViewById(R.id.crypt_activity_note_btn_1);
        btn2 = (Button)findViewById(R.id.crypt_activity_note_btn_2);
        btn3 = (Button)findViewById(R.id.crypt_activity_note_btn_3);
        btn4 = (Button)findViewById(R.id.crypt_activity_note_btn_4);
        btn5 = (Button)findViewById(R.id.crypt_activity_note_btn_5);
        btn6 = (Button)findViewById(R.id.crypt_activity_note_btn_6);
        btn7 = (Button)findViewById(R.id.crypt_activity_note_btn_7);
        btn8 = (Button)findViewById(R.id.crypt_activity_note_btn_8);
        btn9 = (Button)findViewById(R.id.crypt_activity_note_btn_9);
        btn0 = (Button)findViewById(R.id.crypt_activity_note_btn_0);
        doneBtn = (ImageButton)findViewById(R.id.crypt_activity_note_btn_done);
        delBtn = (ImageButton)findViewById(R.id.crypt_activity_note_btn_del);
        text = (TextView)findViewById(R.id.crypt_activity_tv_number);
        toolbar = (Toolbar)findViewById(R.id.crypt_activity_note_layout_toolbar);
        title = (TextView)findViewById(R.id.crypt_activity_note_title);
        setSupportActionBar(toolbar);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        btn0.setOnClickListener(this);

        delBtn.setOnClickListener(this);
        doneBtn.setOnClickListener(this);

        if (savedInstanceState != null){
            text.setText(savedInstanceState.get("TEXT").toString());
        }

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("");
        }

        CloudManager.downloadNotesFromCloud(getApplicationContext());

        int notesCount = DatabaseManager.getNotesCount(getApplicationContext());
        boolean notesAreDecrypted = PreferenceManager.areEncrypted(getApplicationContext());

        if (notesCount == 0) {
            finish();
            Intent moveOnIntent = new Intent(getApplicationContext(), NotesListActivity.class);
            moveOnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(moveOnIntent);
        }

        if (startIntent != null){
            boolean cameFromNotesListActivity = startIntent.getBooleanExtra("CAME_FROM_NOTES_LIST", false);
            if (!cameFromNotesListActivity){
                //move on to the next activity
                if (notesAreDecrypted){
                    Intent moveOnIntent = new Intent(getApplicationContext(), NotesListActivity.class);
                    moveOnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(moveOnIntent);
                }
            }
        }

        if (PreferenceManager.exitWithoutEncrypt(getApplicationContext())){
                Intent moveOnIntent = new Intent(getApplicationContext(), NotesListActivity.class);
                moveOnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(moveOnIntent);
                PreferenceManager.setExitWithoutEncrypt(getApplicationContext(), false);
        }

        if (PreferenceManager.areEncrypted(getApplicationContext())){
            title.setText("enter password".toLowerCase());
        } else {
            title.setText("set password".toLowerCase());
        }
    }

    @Override
    public void onClick(View v) {
        String currText = text.getText().toString();
        String newString = currText;
        if (v == btn0){
            newString = newString + 0;
            text.setText(newString);
        } else if (v == btn1){
            newString = newString + 1;
            text.setText(newString);
        } else if (v == btn2){
            newString = newString + 2;
            text.setText(newString);
        } else if (v == btn3){
            newString = newString + 3;
            text.setText(newString);
        } else if (v == btn4){
            newString = newString + 4;
            text.setText(newString);
        } else if (v == btn5){
            newString = newString + 5;
            text.setText(newString);
        } else if (v == btn6){
            newString = newString + 6;
            text.setText(newString);
        } else if (v == btn7){
            newString = newString + 7;
            text.setText(newString);
        } else if (v == btn8){
            newString = newString + 8;
            text.setText(newString);
        } else if (v == btn9){
            newString = newString + 9;
            text.setText(newString);
        } else if (v == doneBtn){
            //if this condition becomes true there is no point in
            //making the ecryption/decryption
            if (text.getText().toString() == null ||
                    text.getText().toString().equals("")){
                PreferenceManager.setExitWithoutEncrypt(getApplicationContext(), true);
                finish();
                return;
            }

            if (notesAreEncrypted()){
                cachePassword(text.getText().toString());
                SecretKey key = EncryptManager.getKey(getApplicationContext());
                EncryptManager.decryptAllNotes(getApplicationContext(), text.getText().toString(), key,
                        this, this);
            } else {
                EncryptManager.generateKey(getApplicationContext());
                SecretKey key = EncryptManager.getKey(getApplicationContext());
                EncryptManager.encryptAllNotes(getApplicationContext(), text.getText().toString(), key,
                        this, this);
            }

        } else if (v == delBtn){
            if (text.getText().equals("") ||
                    text.getText().length() == 0 ||
                    text.getText() == null){
            } else {
                String currentText = text.getText().toString();
                String newText = currentText.substring(0, currentText.length()-1);
                text.setText(newText);
            }
        }
    }

    private void cachePassword(String password){
        instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.setLooksGoodPassword(getApplicationContext(),
                password);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (text.getText().toString() == null ||
                text.getText().toString().equals("")){
            PreferenceManager.setExitWithoutEncrypt(getApplicationContext(), true);
        }
    }

    /*
     * See if the notes are encrypted
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("TEXT", text.getText().toString());
    }

    private boolean notesAreEncrypted(){
        boolean areEncrypted = instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.
                areEncrypted(getApplicationContext());
        return areEncrypted;
    }

    @Override
    public void cryptographyOperationStarted(boolean isEncryption) {
        Log.d(TAG, (isEncryption) ? "encryption" : "decryption");
        countNotes = 0;
        maxNotes = DatabaseManager.getNotesCount(getApplicationContext());
    }

    @Override
    public void cryptUpdate(boolean isEncryption) {
        Log.d(TAG, (isEncryption) ? "encryption" : "decryption");
        countNotes++;
        if (countNotes == maxNotes) {
            if (isEncryption) {
                /**
                 * No need to clear the history because the history has been
                 * cleared when starting this activity
                 */
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(), NotesListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
}
