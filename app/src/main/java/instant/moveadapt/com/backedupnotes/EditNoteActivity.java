package instant.moveadapt.com.backedupnotes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Permission;

import instant.moveadapt.com.backedupnotes.Constants;
import instant.moveadapt.com.backedupnotes.Managers.FileManager;
import instant.moveadapt.com.backedupnotes.Managers.NoteManager;
import instant.moveadapt.com.backedupnotes.R;

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
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    if (editText != null) {
                        writer.write(editText.getText().toString());
                    }
                    writer.flush();
                    writer.close();
                    NoteManager.setNoteState(EditNoteActivity.this, position, Constants.STATE_LOCAL);
                    Log.d(TAG, "Text written in file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                file.delete();
            }
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
}
