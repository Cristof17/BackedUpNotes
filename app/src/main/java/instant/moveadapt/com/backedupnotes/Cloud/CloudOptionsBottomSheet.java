package instant.moveadapt.com.backedupnotes.Cloud;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import instant.moveadapt.com.backedupnotes.R;

/**
 * Created by cristof on 14.03.2018.
 */

public class CloudOptionsBottomSheet extends BottomSheetDialog {

    private BottomSheetCallback btmSheetCallback;

    public CloudOptionsBottomSheet(@NonNull Context context) {
        super(context);
    }

    public CloudOptionsBottomSheet(@NonNull Context context, @StyleRes int theme) {
        super(context, theme);
    }

    protected CloudOptionsBottomSheet(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_options_bottom_sheet_layout);

        Button btmSheetLogin = (Button) findViewById(R.id.cloud_options_layout_login);
        Button btmSheetRegister = (Button) findViewById(R.id.cloud_options_layout_create_account);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        FrameLayout bottomSheet = (FrameLayout)findViewById(android.support.design.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);

        btmSheetLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
                btmSheetCallback.onOneOfTheOptionsSelected("login");
            }
        });

        btmSheetRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dismiss();
                btmSheetCallback.onOneOfTheOptionsSelected("register");
            }
        });
    }

    public void setBottomSheetCallback(BottomSheetCallback callback){
        this.btmSheetCallback = callback;
    }
}
