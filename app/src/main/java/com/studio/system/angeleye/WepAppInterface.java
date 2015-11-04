package com.studio.system.angeleye;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

public class WepAppInterface {
    Context mContext;
    private LocalStorage localStorage;
    private String authKey;
    InputStream is = null;
    double timeInit;
    private IntentFilter myFilter;
    ProgressDialog pd;
    private String URL_log = "http://157.7.137.182:10047/auth/login/staff_id/";
    int touchCount = 0;

    public WepAppInterface(Context mContext){ this.mContext = mContext;}

    @JavascriptInterface
    public void emergencyCall(String hour){
        Calendar c = Calendar.getInstance();
        int seconds = c.get(Calendar.SECOND);

        touchCount ++;

        switch (touchCount) {
            case 1:
                timeInit = seconds;
                Log.d("CALL", "first touch =  " + timeInit);
                break;
            case 2:
                if(seconds-timeInit<2 && seconds-timeInit>-2){
                    Log.d("CALL","two touch =  "+(int)(seconds-timeInit));
                    timeInit = seconds;
                }else
                    touchCount=0;
                break;
            case 3:
                if(seconds-timeInit<2 && seconds-timeInit>-2) {
                    /*Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("SEND_MSG");
                    broadcastIntent.putExtra("category", "call");
                    broadcastIntent.putExtra("hour", hour);
                    mContext.sendBroadcast(broadcastIntent);*/
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("SEND_MSG");
                    broadcastIntent.putExtra("message","EMERGENCYYY!!");
                    mContext.sendBroadcast(broadcastIntent);
                    Log.d("CALL","emergency!!!!!!");
                    Log.d("DATE",hour);
                }
                touchCount=0;
                break;
        }
    }

    @JavascriptInterface
    public void call(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("tel:"));
        Log.d("call","nothing");
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public void reservation(String date){
        localStorage = new LocalStorage(mContext);
        String id =localStorage.getRoom();
        String auth = convertSHA256("_angel_eye_"+id);
        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(URL_log + id + "/auth_key/" + auth + "/date/" + date);
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }catch(IOException e){
                Toast toast1 = Toast.makeText(mContext,e.toString(), Toast.LENGTH_SHORT);
                toast1.show();
            }finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            String info = sb.toString();

            Log.d("RESERV",info);

        }catch(Exception e){
            Log.e("log_tag", "Error in http connection " + e.toString());
        }
    }

    @JavascriptInterface
    public void sendOver(String msg, String hour){
        /*Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("SEND_MSG");
        broadcastIntent.putExtra("category","termination");
        broadcastIntent.putExtra("hour",hour);
        broadcastIntent.putExtra("message", msg);
        mContext.sendBroadcast(broadcastIntent);*/
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("SEND_MSG");
        broadcastIntent.putExtra("message",msg);
        mContext.sendBroadcast(broadcastIntent);
    }

    @JavascriptInterface
    public String msgHistory(){
        String shop_id;
        String user_id;
        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://angeleye.syoriki.jp/app/message?shop_id="+2+"&user_id="+31+"&page="+1);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;

            try {
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }catch(IOException e){
                Toast toast1 = Toast.makeText(mContext,e.toString(), Toast.LENGTH_SHORT);
                toast1.show();
            }finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            String info = sb.toString();

            JSONArray history = new JSONArray(info);

            Log.d("HISTORY",history.toString());

            return history.toString();

        }catch(Exception e){
            Log.e("log_tag", "Error in http connection "+e.toString());
            return null;
        }
    }

    @JavascriptInterface
    public void sendMsg(String msg, String hour){
        Intent broadcastIntent = new Intent();
        //broadcastIntent.putExtra("category","sns");
        broadcastIntent.setAction("SEND_MSG");
        broadcastIntent.putExtra("message", msg);
        //broadcastIntent.putExtra("date",hour);
        mContext.sendBroadcast(broadcastIntent);
    }

    @JavascriptInterface
    public boolean progress(final String id, final String password){
        pd = new ProgressDialog(mContext);
        pd.setTitle("Please wait");
        pd.setMessage("Page is loading..");
        if( checkLoginData(id)==true){
            pd.show();
            authKey = convertSHA256("_angel_eye_"+password);
            new Thread()
            {
                public void run()
                {
                    try
                    {
                        sleep(2000);
                    }
                    catch (Exception e)
                    {
                        Log.e("tag", e.getMessage());
                    }
                    pd.dismiss();
                }
            }.start();

            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL_log + id +"/password/"+authKey);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
                BufferedReader br = null;
                StringBuilder sb = new StringBuilder();

                String line;

                try {
                    br = new BufferedReader(new InputStreamReader(is));
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }catch(IOException e){
                    Toast toast1 = Toast.makeText(mContext,e.toString(), Toast.LENGTH_SHORT);
                    toast1.show();
                }finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                String info = sb.toString();

                String mssg;
                String val;
                int ans;

                JSONObject user = new JSONObject(info);
                val = user.getString("result");
                mssg = user.getString("result_msg");
                ans = Integer.parseInt(val);

                if(ans == 0){
                    new Thread(){
                        @Override
                        public void run() {
                            localStorage = new LocalStorage(mContext);
                            localStorage.insertUser(id, password,1);
                        }
                    }.start();
                    Intent service = new Intent(mContext,MessageService.class);
                    mContext.startService(service);
                    return true;
                }else{
                    Toast toast1 = Toast.makeText(mContext,mssg,Toast.LENGTH_LONG);
                    toast1.show();
                    return false;
                }
            }catch(Exception e){
                Log.e("log_tag", "Error in http connection " + e.toString());
                return false;
            }

        }else{
            return false;
        }
    }

    @JavascriptInterface
    public boolean checkLoginData(String username) {

        if (username.equals("")) {
            Toast toast1 = Toast.makeText(mContext,"ログインIDが入力されていません", Toast.LENGTH_SHORT);
            toast1.show();
            return false;

        } else {

            return true;
        }
    }

    @JavascriptInterface
    public void startService(){
        Intent service = new Intent(mContext,MessageService.class);
        mContext.startService(service);
    }

    @JavascriptInterface
    public void logOut(){
        localStorage = new LocalStorage(mContext);
        localStorage.deleteUser();
        Intent service = new Intent(mContext,MessageService.class);
        mContext.stopService(service);
        Intent intent = new Intent(mContext,MainActivity.class);
        mContext.startActivity(intent);
    }

    @JavascriptInterface
    public static String convertSHA256(String text) {

        // 変数初期化
        MessageDigest md = null;
        StringBuffer buffer = new StringBuffer();

        try {
            // メッセージダイジェストインスタンス取得
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // 例外発生時、エラーメッセージ出力
        }

        // メッセージダイジェスト更新
        md.update(text.getBytes());

        // ハッシュ値を格納
        byte[] valueArray = md.digest();

        // ハッシュ値の配列をループ
        for(int i = 0; i < valueArray.length; i++){
            // 値の符号を反転させ、16進数に変換
            String tmpStr = Integer.toHexString(valueArray[i] & 0xff);

            if(tmpStr.length() == 1){
                // 値が一桁だった場合、先頭に0を追加し、バッファに追加
                buffer.append('0').append(tmpStr);
            } else {
                // その他の場合、バッファに追加
                buffer.append(tmpStr);
            }
        }

        // 完了したハッシュ計算値を返却
        return buffer.toString();
    }
}
