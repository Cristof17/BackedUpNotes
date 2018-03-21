package instant.moveadapt.com.backedupnotes.Cloud;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 14.03.2018.
 */

public class CloudCredentialsBottomSheet extends BottomSheetDialog{

    private String buttonText;
    private BottomSheetCallback callback;

    public CloudCredentialsBottomSheet(@NonNull Context context) {
        super(context);
    }

    public CloudCredentialsBottomSheet(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
    }

    protected CloudCredentialsBottomSheet(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setActionButtonText(String text){
        this.buttonText = text;
    }

    public void setLoginCallback(BottomSheetCallback callback){
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_credentials_bottom_sheet);

        final EditText usernameText = (EditText) findViewById(R.id.cloud_credentials_bottom_sheet_username);
        final EditText passwordText = (EditText) findViewById(R.id.cloud_credentials_bottom_sheet_password);
        Button actionButton = (Button) findViewById(R.id.cloud_credentials_bottom_sheet_btn);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);

        FrameLayout bottomSheet = (FrameLayout)findViewById(android.support.design.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);

        actionButton.setText(buttonText);
        actionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (buttonText.toLowerCase().equals("login")) {
                    if (callback != null) {
                        callback.onLoginSelected(usernameText.getText().toString(), passwordText.getText().toString());
                        dismiss();
                    }
                } else if (buttonText.toLowerCase().equals("register")){
                    if (callback != null) {
                        callback.onRegisterSelected(usernameText.getText().toString(), passwordText.getText().toString());
                        dismiss();
                    }
                }
            }
        });
    }


}
