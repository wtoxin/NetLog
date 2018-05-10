package edu.nudt.netlog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author ainassine
 * @version 0.0.1
 * @date 2018-04-21
 * created for timing tasks, use this class to receive timing msg
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, TimerService.class);
        context.startService(i);
    }
}
