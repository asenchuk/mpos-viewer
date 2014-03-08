package net.taviscaron.mposviewer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import net.taviscaron.mposviewer.R;

import java.io.Closeable;
import java.io.IOException;

/**
 * Different IO util
 * @author Andrei Senchuk
 */
public class IOUtils {
    private static final String TAG = "IOUtils";

    public static void close(Closeable c) {
        if(c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.w(TAG, e);
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        return isNetworkAvailable(context, false);
    }

    public static boolean isNetworkAvailable(Context context, boolean showToastAllert) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkAvailable = (activeNetworkInfo != null && activeNetworkInfo.isConnected());

        if(!isNetworkAvailable && showToastAllert) {
            Toast.makeText(context, R.string.error_network_unavailable, Toast.LENGTH_SHORT).show();
        }

        return isNetworkAvailable;
    }
}
