package instant.moveadapt.com.backedupnotes.Preference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import instant.moveadapt.com.backedupnotes.Cloud.CloudManager;
import instant.moveadapt.com.backedupnotes.R;

public class LoginPreference extends Preference{
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
        ImageView icon = (ImageView)rootView.findViewById(R.id.login_preference_layout_icon);

        if (CloudManager.isLoggedIn()){
            title.setTextColor(getContext().getColor(R.color.logged_in));
            icon.setImageResource(R.drawable.ic_ok_appproval_acceptance);
            title.setText("Logged in as "+ " " + CloudManager.getLoggedInEmail());
        } else {
            title.setTextColor(getContext().getColor(R.color.logged_out));
            icon.setImageResource(R.drawable.ic_account);
            title.setText("Create a storage accout");
        }
        return rootView;
    }
}
