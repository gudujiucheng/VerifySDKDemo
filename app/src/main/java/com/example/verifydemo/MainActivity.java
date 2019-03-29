package com.example.verifydemo;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.cert.X509Certificate;


import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    String mTestUrl = "https://support.captcha.qq.com/cgi-bin/open_cap/test.pl";//第三方业务使用验证码需替换此url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button)findViewById(R.id.verifybt_fullscreen)).setOnClickListener(this);
        ((Button)findViewById(R.id.verifybt_popup)).setOnClickListener(this);
    }

    private void gotoVerifyFullScreenActivity(String jsurl){
        VerifyCodeDialogFragment.newInstance(jsurl).show(getSupportFragmentManager(),"xxxx");
//        try {
//            Log.e("url=", jsurl);
//            Intent it = new Intent(this,VerifyFullScreenActivity.class);
//            it.putExtra("jsurl", jsurl);
//            startActivityForResult(it,1);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }
    private void gotoVerifyPopupActivity(String jsurl){
        try {
            Log.e("url=", jsurl);
            Intent it = new Intent(this, VerifyPopupActivity.class);
            it.putExtra("jsurl", jsurl);
            startActivityForResult(it,1);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    protected void onResume(){
        super.onResume();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode== Activity.RESULT_OK){
                Log.e("onActivityResult", "verifysucc");
                String ticket = data.getStringExtra("ticket");
                String randstr = data.getStringExtra("randstr");
                Toast.makeText(MainActivity.this, "验证成功,票据为"+ticket,Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(MainActivity.this, "未验证成功",Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android use X509 cert
        X509TrustManager[] trustAllCerts = new X509TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(final View view) {
        switch(view.getId()){
            case R.id.verifybt_fullscreen:
            case R.id.verifybt_popup:
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        trustAllHosts();
                        HttpClient client  =new HttpClient();
                        byte[] response = client.request(MainActivity.this, mTestUrl);
                        if(response==null){//失败了
                            Log.d("requestfail", "response==null");
                            return;
                        }
                        String responseString = new String(response);
                        try {
                            JSONObject jsonObject = new JSONObject(responseString);
                            int code = jsonObject.optInt("code",-1);
                            if(code==0){
                                String url = jsonObject.optString("url");
                                if(url!=null){
                                    if (view.getId() == R.id.verifybt_fullscreen){
                                        gotoVerifyFullScreenActivity(url);
                                    }else{
                                        gotoVerifyPopupActivity(url);
                                    }

                                }
                            }
                            else{//获取jsurl失败
                                String msg = jsonObject.optString("message");
                                Toast.makeText(MainActivity.this, msg,Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                }).start();

                break;
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}