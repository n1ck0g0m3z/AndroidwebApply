package com.studio.system.angeleye;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

    private MyWebView mWebView;
    final Context myApp = this;
    public static boolean flag = false;
    private LocalStorage localStorage;
    private IntentFilter myFilter;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        myFilter = new IntentFilter();
        myFilter.addAction("MSG");

        MyWebView mWebView = (MyWebView) findViewById(R.id.activity_main_webview);
        mWebView.setWebViewClient(new WebViewClient(){
        ProgressDialog pd = null;

        @Override
        public void onPageStarted(WebView view, String url,Bitmap favicon){
            if(url.contains("file:///android_asset/html/map.html")) {
                flag=false;
            }

            if(url.contains("file:///android_asset/html/message.html")){
                Intent broadcast = new Intent();
                broadcast.setAction("SEND_MSG");
                broadcast.putExtra("message","true");
                MainActivity.this.sendBroadcast(broadcast);
            }
            if(!url.contains("file:///android_asset/html/message.html")){
                Intent broadcast = new Intent();
                broadcast.setAction("SEND_MSG");
                broadcast.putExtra("message","false");
                MainActivity.this.sendBroadcast(broadcast);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(url.contains("file:///android_asset/html/map.html")&&flag==true){
                pd.dismiss();
            }

            if(url.contains("file:///android_asset/html/map.html")&&flag==false) {
                pd = new ProgressDialog(myApp);
                pd.setTitle("Please wait");
                pd.setMessage("Page is loading..");
                pd.show();
                view.loadUrl("javascript:initialize()");
                flag = true;

                new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            sleep(3700);
                        }
                        catch (Exception e)
                        {
                            Log.e("tag", e.getMessage());
                        }
                        pd.dismiss();
                    }
                }.start();
            }
        }
        });
        mWebView.addJavascriptInterface(new WepAppInterface(this), "Android");
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(myApp)
                        .setTitle("リサイクルシステム")
                        .setMessage(message)
                        .setPositiveButton("はい",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("キャンセル",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .create()
                        .show();

                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        localStorage = new LocalStorage(this);
        cursor = localStorage.getUsers();

        if(cursor.getCount()==0){
            mWebView.loadUrl("file:///android_asset/html/login.html");
        }else mWebView.loadUrl("file:///android_asset/html/index.html");
        Log.d("user",Integer.toString(cursor.getCount()));
        registerReceiver(myReceiver,myFilter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }
    @Override
    protected void onPause(){ super.onPause();}
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU){
            if(!mWebView.getUrl().contains("file:///android_asset/html/login.html"))
            mWebView.loadUrl("file:///android_asset/html/states.html");
        }
        return super.onKeyDown(keyCode, event);
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String myParam = intent.getExtras().getString("message");

            String url = mWebView.getUrl();
            if (myParam != null) {
                if(url.contains("file:///android_asset/html/message.html")){
                    mWebView.loadUrl("javascript:admMsg('"+myParam+"')");
                }
            }
        }
    };
}
