package com.studio.system.angeleye;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;

/**
 * Created by N1cK0 on 15/03/30.
 */
public class MessageService extends Service implements LocationListener {
    boolean isNetworkEnabled = false;
    boolean isGPSenabled = false;
    boolean connection=false;
    Location location;
    boolean onMessage = false;
    double latitude;
    double longitude;
    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private static final int MIN_TIME_BTW_UPDATES = 3000;
    protected LocationManager locationManager;
    PowerManager.WakeLock wl;
    Handler handler;
    LocalStorage localStorage;
    static String room;
    private IntentFilter myFilter;
    private Socket mSocket;
    {
        try{
            mSocket = IO.socket("http://157.7.137.182:3000");
        } catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        localStorage = new LocalStorage(this);
        if(localStorage.getUsers().getCount()>0) {
            myFilter = new IntentFilter();
            myFilter.addAction("SEND_MSG");
            handler = new Handler();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        room = localStorage.getRoom();
        /*final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("room_id", room);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("join room", jsonObject.toString());*/
        mSocket.emit("join room",room);
        mSocket.connect();
        mSocket.on("chat message", onNewMessage);
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        registerReceiver(myReceiver, myFilter);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MessageService");
        wl.acquire();
        getLocation();
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(myReceiver);
        stopGPS();
        wl.release();
        mSocket.off("chat message", onNewMessage);
        mSocket.disconnect();
    }

    public void getLocation(){
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        //isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if( !isGPSenabled && !isNetworkEnabled) {
            Log.d("DISABLED BOTH","disabled");
        }else{
            connection = true;
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BTW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("network", "network ok");
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
            /*if (isGPSenabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BTW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Log.d("network", "GPS");
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        Log.d("GPS", latitude + "   " + longitude);
                    }
                }
            }*/
        }
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private void createNotification(String msg){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("MSG");
        broadcastIntent.putExtra("message", msg);
        this.sendBroadcast(broadcastIntent);
        if(onMessage==false) {
            NotificationManager nNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icon_arrow)
                    .setContentTitle("NEW MESSAGE")
                    .setVibrate(new long[]{100, 200, 100, 500})
                    .setLights(Color.CYAN, 1000, 1000)
                    .setContentText(msg)
                    .setDefaults(Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_SOUND)
                    .setPriority(Notification.PRIORITY_MAX);

            Intent intent = new Intent(this, WebViewC.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true).setWhen(System.currentTimeMillis());
            nNM.notify(0, mBuilder.build());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        /*JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("category", "location");
            jsonObject.put("room_id", room);
            jsonObject.put("shop_id","");
            jsonObject.put("time","");
            jsonObject.put("x",longitude);
            jsonObject.put("y",latitude);
            mSocket.emit("chat message",jsonObject.toString());
            Log.d("Position", latitude + "   " + longitude);
        }catch (JSONException e){
            Log.e("ERROR",e.toString());
        }*/
        mSocket.emit("chat message","Position" + latitude + "   " + longitude);
        Log.d("Position", latitude + "   " + longitude);
    }

    @Override
    public void onProviderDisabled(String provider) {
        /*Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,0);
        NotificationManager nNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon_map)
                .setContentTitle("位置情報サービスが無効です")
                .setVibrate(new long[]{100, 200, 100, 500})
                .addAction(R.drawable.icon_states,"設定",pendingIntent)
                .setLights(Color.CYAN, 1000, 1000)
                .setContentText("現在地情報を利用します。位置情報サービスをオンにしてください。")
                .setDefaults(Notification.FLAG_SHOW_LIGHTS | Notification.DEFAULT_SOUND)
                .setPriority(Notification.PRIORITY_MAX);

        mBuilder.setAutoCancel(true).setWhen(System.currentTimeMillis());
        nNM.notify(1, mBuilder.build());*/
        Log.d("DISABLED","disabled");
        getLocation();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("ENABLED","enabled");
        getLocation();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private void stopGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(MessageService.this);
            Log.d("STOP","stop");
        }
    }

    private void decryptedMsg(String data){
        switch (data){
            case "start":
                getLocation();
                break;
            case "end":
                stopGPS();
                break;
            default:
                createNotification(data);
                break;
        }
        /*String category = "";
        try {
            JSONObject jsonObject = new JSONObject(data);
            category = jsonObject.getString("category");
            if(category.equals("sns")) {
                createNotification(data);
            }else
            if(category.equals("location")){
                String order = jsonObject.getString("order");
                if(order.equals("start")){
                    getLocation();
                }else
                if(order.equals("end")){
                    stopGPS();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = (String) args[0];
                    decryptedMsg(data);
                }
            });
        }
    };

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String myParam = intent.getExtras().getString("message");
            if(myParam.equals("true")){
                onMessage=true;
                NotificationManager nt = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nt.cancel(0);
            }else{
                onMessage=false;
            }
            mSocket.emit("chat message",myParam);
            /*String category = intent.getExtras().getString("category");
            JSONObject jsonObject = new JSONObject();
            String chatMessage;
            String myHour;

            if (myParam != null) {

                switch (category){

                    case "sns":
                        String time = intent.getExtras().getString("date");
                        try {
                            jsonObject.put("category", category);
                            jsonObject.put("room_id", room);
                            jsonObject.put("message", myParam);
                            jsonObject.put("time", time);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        chatMessage = jsonObject.toString();
                        if (TextUtils.isEmpty(chatMessage)) return;
                        mSocket.emit("chat message", chatMessage);
                        break;

                    case "termination":
                        myHour = intent.getExtras().getString("hour");
                        try {
                            jsonObject.put("category",category);
                            jsonObject.put("room_id",room);
                            jsonObject.put("message",myParam);
                            jsonObject.put("shop_id","");
                            jsonObject.put("time",myHour);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        chatMessage = jsonObject.toString();
                        if(TextUtils.isEmpty(chatMessage))return;
                        mSocket.emit("chat message",chatMessage);
                        break;

                    case "call":
                        myHour = intent.getExtras().getString("hour");
                        try {
                            jsonObject.put("category",category);
                            jsonObject.put("room_id",room);
                            jsonObject.put("order_id","");
                            jsonObject.put("shop_id","");
                            jsonObject.put("time",myHour);
                            jsonObject.put("x","");
                            jsonObject.put("y","");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        chatMessage = jsonObject.toString();
                        if(TextUtils.isEmpty(chatMessage))return;
                        mSocket.emit("chat message",chatMessage);
                        break;

                    default:
                        break;

                }
            }*/
        }
    };
}

