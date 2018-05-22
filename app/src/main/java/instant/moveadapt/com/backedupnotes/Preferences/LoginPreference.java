package instant.moveadapt.com.backedupnotes.Preferences;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import instant.moveadapt.com.backedupnotes.Cloud.CloudManager;
import instant.moveadapt.com.backedupnotes.Crypt;
import instant.moveadapt.com.backedupnotes.LoginActivity;
import instant.moveadapt.com.backedupnotes.R;

public class LoginPreference extends Preference{

    private static final String TAG = "LOGIN_PREFERENCE";

    public LoginPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LoginPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LoginPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoginPreference(Context context) {
        super(context);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        LayoutInflater inflater = getContext().getSystemService(LayoutInflater.class);
        View rootView = inflater.inflate(R.layout.login_preference_layout, parent, false);
        TextView title = (TextView)rootView.findViewById(R.id.login_preference_layout_title);
        TextView summary = (TextView)rootView.findViewById(R.id.login_preference_layout_summary);
        Button value = (Button)rootView.findViewById(R.id.login_preference_layout_value);
        ImageView icon = (ImageView)rootView.findViewById(R.id.login_preference_layout_icon);

        if (CloudManager.isLoggedIn()){
            title.setTextColor(getContext().getColor(R.color.logged_in));
            icon.setImageResource(R.drawable.ic_ok_appproval_acceptance);
            title.setText(CloudManager.getLoggedInEmail());
            value.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    CloudManager.logout();
                }
            });
            value.setVisibility(View.VISIBLE);
            summary.setVisibility(View.GONE);
        } else {
            title.setTextColor(getContext().getColor(R.color.logged_out));
            icon.setImageResource(R.drawable.ic_account);
            title.setText("Create a storage account");
            value.setVisibility(View.GONE);
            summary.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    protected void onClick() {
        Log.d(TAG, "OnClick()");
        Intent cloudActivityIntent = new Intent(getContext(), LoginActivity.class);
//        cloudActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getContext().startActivity(cloudActivityIntent);
    }
}
