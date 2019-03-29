package com.example.verifydemo;
import com.token.verifysdk.VerifyCoder;
import com.token.verifysdk.VerifyCoder.VerifyListener;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;

public class VerifyFullScreenActivity extends Activity {
    private WebView mWebView;
    private VerifyListener listener = new VerifyListener() {

        @Override
        public void onVerifySucc(String ticket, String randstr) {
            // TODO Auto-generated method stub
            Intent it = new Intent();
            it.putExtra("ticket", ticket);
            it.putExtra("randstr", randstr);
            setResult(Activity.RESULT_OK, it);
            finish();
        }

        @Override
        public void onVerifyFail() {
            // TODO Auto-generated method stub
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        @Override
        public void onIframeLoaded(int state, String info) {

        }

        @Override
        public void onIFrameResize(float width, float height) {
            //全屏验证码可不用处理该方法
        }


    };

    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        String content2 = getIntent().getStringExtra("jsurl");
        if (content2 == null) {
            finish();
            return;
        }
        VerifyCoder verify = VerifyCoder.getVerifyCoder();
        verify.setShowtitle(true);
        mWebView = verify.getWebView(getApplicationContext(), content2, listener);
        mWebView.requestFocus();
        mWebView.forceLayout();
        setContentView(mWebView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.clearCache(true);
            mWebView.freeMemory();
            ViewGroup parent = (ViewGroup) mWebView.getParent();
            if (parent != null) {
                parent.removeView(mWebView);
            }
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
        }
        VerifyCoder.getVerifyCoder().release();
    }

}
