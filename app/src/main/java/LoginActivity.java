import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import instant.moveadapt.com.backedupnotes.R;

public class LoginActivity extends Activity {

    private static final String TAG = "LOGIN_ACTIVITY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
    }
}
