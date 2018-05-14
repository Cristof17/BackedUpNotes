package instant.moveadapt.com.backedupnotes;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import instant.moveadapt.com.backedupnotes.Database.NotesDatabase;
import instant.moveadapt.com.backedupnotes.Encrypt.EncryptManager;
import instant.moveadapt.com.backedupnotes.Pojo.Note;
import instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager;

import javax.crypto.SecretKey;

/**
 * Created by cristof on 11.05.2018.
 */

public class Crypt extends Activity implements View.OnClickListener{

    Button btn0;
    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;
    Button btn5;
    Button btn6;
    Button btn7;
    Button btn8;
    Button btn9;
    Button delBtn;
    Button doneBtn;
    TextView text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        doneBtn = (Button)findViewById(R.id.crypt_activity_note_btn_done);
        delBtn = (Button)findViewById(R.id.crypt_activity_note_btn_del);
        text = (TextView)findViewById(R.id.crypt_activity_tv_number);

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
            if (text.getText() == null ||
                    text.getText().equals(""))
                return;

            if (notesAreEncrypted()){
                cachePassword(text.getText().toString());
                SecretKey key = EncryptManager.getKey(getApplicationContext());
                EncryptManager.decryptAllNotes(getApplicationContext(), text.getText().toString(), key);
                finish();
            } else {
                EncryptManager.generateKey(getApplicationContext());
                SecretKey key = EncryptManager.getKey(getApplicationContext());
                EncryptManager.encryptAllNotes(getApplicationContext(), text.getText().toString(), key);
                finish();
            }

        } else if (v == delBtn){
            if (text.getText().equals("") ||
                    text.getText().length() == 0 ||
                    text.getText() == null){
                Toast.makeText(getApplicationContext(), "Nothing to delete", Toast.LENGTH_SHORT).show();
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

    /*
     * See if the notes are encrypted
     */
    private boolean notesAreEncrypted(){
        boolean areEncrypted = instant.moveadapt.com.backedupnotes.Preferences.PreferenceManager.
                areEncrypted(getApplicationContext());
        return areEncrypted;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("TEXT", text.getText().toString());
    }
}
