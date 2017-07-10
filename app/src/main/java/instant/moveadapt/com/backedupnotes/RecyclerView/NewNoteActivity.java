package instant.moveadapt.com.backedupnotes.RecyclerView;

import android.Manifest;
import android.app.Activity;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Permission;

import instant.moveadapt.com.backedupnotes.Constants;
import instant.moveadapt.com.backedupnotes.Managers.FileManager;
import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 13.06.2017.
 */

public class NewNoteActivity extends AppCompatActivity {

    public static final String TAG = "[NEW_NOTE_ACTIVITY]";

    private Toolbar toolbar;
    private EditText editText;
    private File newNoteFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_new_note_layout);
        toolbar = (Toolbar)findViewById(R.id.new_note_toolbar);
        editText = (EditText)findViewById(R.id.new_note_edit_text);

        newNoteFile = FileManager.createNewNoteFile(NewNoteActivity.this);
        newNoteFile.setReadable(true);
        newNoteFile.setWritable(true);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Toast.makeText(NewNoteActivity.this, "Focus = " + b, Toast.LENGTH_SHORT).show();
            }
        });

        if (ContextCompat.checkSelfPermission(NewNoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(NewNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_WRITE_PERMISSION_REQ_CODE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        /*
            Save the note
         */
        if (newNoteFile != null){
            if (editText.getText().toString() != null && !editText.getText().toString().equals("")) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(newNoteFile));
                    if (editText != null) {
                        writer.write(editText.getText().toString());
                    }
                    writer.flush();
                    writer.close();
                    Log.d(TAG, "Text written in file");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                newNoteFile.delete();
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
