package otmsapp.ping.tools;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressFactory {
    private ProgressFactory(){};
    public static ProgressDialog createSimpleDialog(Context context, String message){
        ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
        return progressDialog;
    }
}
