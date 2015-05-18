package goodline.info.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Крабов on 17.05.2015.
 */
public class NewsBroadcastReceiver extends BroadcastReceiver {

    private static final String DEBUG_TAG = "SimpleBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(DEBUG_TAG, "Simple broadcast received");
    }
}