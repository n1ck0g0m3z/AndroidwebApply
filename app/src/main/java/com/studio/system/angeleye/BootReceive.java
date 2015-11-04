package com.studio.system.angeleye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by N1cK0 on 15/03/30.
 */
public class BootReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, MessageService.class);
        //service.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(service);
    }
}
