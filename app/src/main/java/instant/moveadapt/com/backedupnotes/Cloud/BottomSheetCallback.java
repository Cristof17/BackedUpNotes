package instant.moveadapt.com.backedupnotes.Cloud;

/**
 * Created by cristof on 14.03.2018.
 */

public interface BottomSheetCallback {

    public void onLoginSelected(String email, String password);
    public void onRegisterSelected(String email, String password);
    public void onOneOfTheOptionsSelected(String optionText);

}
