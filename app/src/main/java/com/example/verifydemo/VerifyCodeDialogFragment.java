package com.example.verifydemo;

/**
 * @Description:
 * @Author: canzhang
 * @CreateDate: 2019/3/28 16:21
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.token.verifysdk.VerifyCoder;

public class VerifyCodeDialogFragment extends DialogFragment {
    public static final String JS_URL = "js_url";
    private WebView mWebView;
    private RelativeLayout mContainer;
    private ProgressBar mProgressBar;
    private Context mContext;
    private float mScale = 0.7f; //默认弹框验证码宽度是屏幕宽度*0.7
    private int mIframeHeightPX;
    private int mIframeWidthPX;


    private final float F_DEFAULT_POPUP_IFRAME_WIDTH = 18.2f * 16;
    private final int F_MAX_IFRAME_WIDTH_SCALE = 2;
    private final int F_CAP_TYPE_CLICK_CHAR_ZH = 4;//图中点字(中文)
    private final int F_CAP_TYPE_CLICK_CHAR_EN = 6;//图中点字(英文)
    private final int F_CAP_TYPE_SLIDE_PUZZLE = 7;//滑动拼图
    private  float mDensity ;
    private VerifyCoder.VerifyListener mVerifyListener = new VerifyCoder.VerifyListener() {
        public void onVerifySucc(String ticket, String randstr) {

            dismiss();
        }

        public void onVerifyFail() {

            dismiss();
        }

        @Override
        public void onIframeLoaded(int i, String s) {
            //收到验证码页面(包括图片)加载完成回调时，把Loading隐藏，WebView显示
            mProgressBar.setVisibility(View.INVISIBLE);
            mWebView.setVisibility(View.VISIBLE);
//            getDialog().getWindow().setLayout(mIframeWidthPX ,ViewGroup.LayoutParams.WRAP_CONTENT );
        }

        @Override
        public void onIFrameResize(float width, float height) {
            android.view.WindowManager.LayoutParams attributes =  getDialog().getWindow().getAttributes();
            attributes.width = (int)(width*mDensity);
            attributes.height = (int)(height*mDensity);
            getDialog().getWindow().setAttributes(attributes);
        }
    };

    public static VerifyCodeDialogFragment newInstance(String jsUrl) {
        Bundle args = new Bundle();
        args.putString(JS_URL, jsUrl);
        VerifyCodeDialogFragment fragment = new VerifyCodeDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (arguments != null) {
            String jsUrl = arguments.getString(JS_URL);
            if (jsUrl == null) {
                dismiss();
            }
            View view = inflater.inflate(R.layout.activity_verify_popup, container);

            int windowWidth = getWindowWidth(mContext);
            WindowManager manager = ((Activity)mContext).getWindowManager();
            DisplayMetrics metrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(metrics);
            mDensity = metrics.density;
            /*
             * 以滑动拼图弹框验证码为例，取弹框验证码宽度为屏幕宽度0.7
             * 滑动拼图标准宽18.2*16dp，标准高16.1*16dp,最大缩放比例2 ----capType=7
             * 图中点字标准宽18.2*16dp，标准高19.6*16dp,最大缩放比例2 ----capType=4,6
             * */
            mIframeWidthPX = (int) (windowWidth * mScale);
            int iFrameWidthDP = px2dip(mContext, mIframeWidthPX);
            Log.e("Test", "宽度：mIframeWidthPX：" + mIframeWidthPX + " iFrameWidthDP:" + iFrameWidthDP);
            //标准值限制
            if (iFrameWidthDP >= (int) (F_DEFAULT_POPUP_IFRAME_WIDTH * F_MAX_IFRAME_WIDTH_SCALE)) {
                iFrameWidthDP = (int) (F_DEFAULT_POPUP_IFRAME_WIDTH * F_MAX_IFRAME_WIDTH_SCALE);
                mIframeWidthPX = (int) dip2px(mContext, iFrameWidthDP);
                Log.e("Test", "宽度超出极限值更新：mIframeWidthPX：" + mIframeWidthPX + " iFrameWidthDP:" + iFrameWidthDP);
            }
            //根据验证码类型和弹框宽度，获取验证码弹框高度
            int iFrameHeightDP = VerifyCoder.getPopupIframeHeightByWidthAndCaptype(iFrameWidthDP, F_CAP_TYPE_SLIDE_PUZZLE);
            mIframeHeightPX = (int) dip2px(mContext, iFrameHeightDP);
            Log.e("Test", "高度：mIframeHeightPX：" + mIframeHeightPX + " iFrameHeightDP:" + iFrameHeightDP);

            //设置主题色，弹框验证码，弹框宽度
            VerifyCoder verifyCoder = VerifyCoder.getVerifyCoder();
            verifyCoder.setJson("themeColor:'3180FF',type:'popup',fwidth:" + iFrameWidthDP);


            mWebView = verifyCoder.getWebView(getContext(), jsUrl, mVerifyListener);
            mWebView.requestFocus();
            mWebView.forceLayout();
            mContainer = (RelativeLayout) view.findViewById(R.id.container);
            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            mWebView.setVisibility(View.INVISIBLE);
            mContainer.addView(mWebView);
            mWebView.setHorizontalScrollBarEnabled(false);
            mWebView.setVerticalScrollBarEnabled(false);

            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    Log.e("Test", "高度：height：" + mWebView.getHeight() + " width:" + mWebView.getWidth());
                }
            });

            final Window window = getDialog().getWindow();
            android.view.WindowManager.LayoutParams attributes =window.getAttributes();
            attributes.width = mIframeWidthPX;
            attributes.height = mIframeHeightPX;
            window.setAttributes(attributes);
//            if (window != null ) {
//                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                android.view.WindowManager.LayoutParams attributes = window.getAttributes();
//                attributes.width = mIframeWidthPX;
//                attributes.height = mIframeHeightPX;
//                window.setAttributes(attributes);
//            }


            return view;
        } else {
            dismiss();
            return new View(getContext());
        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    @Override
    public void onStart() {
        super.onStart();
//        Dialog dialog = getDialog();
//        if (dialog != null ) {
//            final Window window = getDialog().getWindow();
//            if (window != null) {
//                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                Log.e("Test", "mIframeWidthPX:" + mIframeWidthPX + " mIframeHeightPX:" + mIframeHeightPX);
//                window.setLayout(mIframeWidthPX,mIframeHeightPX);
//            }
//        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
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




    /**
     * reverse dp to px
     */
    public static float dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }

    /**
     * reverse px to dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static int getWindowWidth(Context context) {

        WindowManager wm = (WindowManager) (context
                .getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;

    }


}