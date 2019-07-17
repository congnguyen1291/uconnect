package vn.urekamedia.liboverlay;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.andremion.counterfab.CounterFab;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vn.urekamedia.liboverlay.services.ApiCall;
import vn.urekamedia.liboverlay.videoPlayer.VideoPlayer;

import static vn.urekamedia.liboverlay.Utils.getSerialNumber;
import static vn.urekamedia.liboverlay.Utils.getSerialNumber;

public class FloatingWidgetService extends Service {

    private static final String TODO = "";

    public static final String SECTION_PREROLL = "preroll";
    public static final String SECTION_POSTROLL = "postroll";
    public static final String SECTION_MIDROLL = "midroll";
    private WindowManager mWindowManager;
    private WebView wv1;
    private Thread thread;

    public long timeShowAds=30000;
    public boolean isRunning = false;
    int mWidth;
    CounterFab counterFab;

    private int currentPosition;

    private int position = 0;
    private MediaController mediaController;
    public static View mOverlayView;
    boolean activity_background;
    public String macId=GetMac.getMacID();
    public String location="Ho Chi Minh";
    RelativeLayout myVideoView;
    String TAG = "VideoPlayer";

    String videoSource = "https://cdn.urekamedia.vn/global/ureka2.mp4";
    Uri uriVideoSource;

    private View videoFragmentView;
    private View webviewFragmentView;
    public static View videoRootView;

    public static VideoPlayer mVideoPlayer;
    public static boolean mIsAdDisplayed;

    public static ViewGroup mAdUiContainer;
    // The video player.
    public static ImaSdkFactory mSdkFactory;
    public static AdsLoader mAdsLoader;

    public static AdsManager mAdsManager;
    public static View VideoLayout;
    private static VideoAdPlayer mVideoAdPlayer;


    // Sending side
    public String device = macId;//macIdEncode;
    public String serialId=getSerialNumber();

    public String partner = "1";
    private String urlJson = "http://tracking.urekamedia.com/api/getads/?partner="+partner+"&deviceid=" + device+"&codeNumber="+serialId;
    public static String AD_UNIT_ID_VIDEO = "<![CDATA[https://pubads.g.doubleclick.net/gampad/ads?iu=/2627062/tv.hanet.com_640x480_video&description_url=[placeholder]&env=vp&impl=s&correlator=&tfcd=0&npa=0&gdfp_req=1&output=vast&sz=1x1|640x480|1300x720&unviewed_position_start=1]]>";

    //

    public static String content_url="https://cdn.urekamedia.vn/global/ureka2.mp4";

    private String urlShowAds = "http://banner.tin8.co/utivi";
    private MyThread mythread;
    private List<VideoAdPlayer.VideoAdPlayerCallback> mAdCallbacks = new ArrayList<>(1);



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //@Override
    @SuppressLint("WrongViewCast")
    public int onStartCommand(Intent intent, int flags, int startId) {

        mythread  = new MyThread();
        if(!isRunning){
            mythread.start();
            isRunning = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();

        if (mOverlayView != null){
            mWindowManager.removeView(mOverlayView);
        }
    }

    public boolean CallWebView(final String timeshow, final int positionid, final  int width, final int height) {

            WebView myWebView = (WebView) mOverlayView.findViewById(R.id.webview);
            myWebView.setBackgroundColor(0x00000000);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setPluginState(WebSettings.PluginState.ON);
            myWebView.getSettings().setJavaScriptEnabled(true);
            webSettings.setUseWideViewPort(true);
            myWebView.setVerticalScrollBarEnabled(false);
            myWebView.setHorizontalScrollBarEnabled(false);
            myWebView.getSettings().setLoadWithOverviewMode(true);
            myWebView.getSettings().setUseWideViewPort(true);
            myWebView.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
            );
            myWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
                @Override
                public void onPageFinished(WebView view, final String url) {
                }
            });
            myWebView.setWebChromeClient(new WebChromeClient());
            Long time_show =Long.parseLong(timeshow);
            myWebView.loadUrl(urlShowAds+"?deviceid="+device+"&time_show="+time_show+"&partner="+partner+"&position="+positionid+"&codeNumber="+serialId);
            Log.i("Link Ads", urlShowAds+"?deviceid="+device+"&time_show="+time_show+"&partner="+partner+"&position="+positionid+"&codeNumber="+serialId);
            return false;
    }
    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    private class GetJSONTask extends AsyncTask<String, Void, String> {
        // progress dialog.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return ApiCall.getData(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve data. URL may be invalid.";
            }
        }

        @SuppressLint("WrongViewCast")
        @Override
        protected void onPostExecute(String result) {
            mOverlayView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.overlay_layout, null);
            videoRootView= (RelativeLayout) mOverlayView.findViewById(R.id.videoLayout);
            mVideoPlayer = (VideoPlayer) videoRootView.findViewById(R.id.VideoPlayer);
            mAdUiContainer = (FrameLayout) videoRootView.findViewById(R.id.adUiContainer);
            // onPostExecute displays the results of the doInBackgroud and also we
            // can hide progress dialog.
            DisplayMetrics displayMetrics = new DisplayMetrics();

            int deviceWidth = displayMetrics.widthPixels;
            int deviceHeight = displayMetrics.heightPixels;
            Context context;
            String type_ads = "1";
            String position_x = "0";
            String position_y = "0";
            String width = String.valueOf(deviceWidth-50);
            String height = "100";
            String time_show = "30000";
            String status = "1";
            String position="0";
            JSONObject json = null;
            try {
                json = new JSONObject(result);
                type_ads = (String) json.get("type_ads");
                position_x = (String) json.get("position_x");
                position_y = (String) json.get("position_y");
                width = (String) json.get("width");
                height = (String) json.get("height");
                time_show = (String) json.get("time_show");
                status = (String) json.get("status");
                position=(String) json.get("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT);

                switch (Integer.parseInt(type_ads)) {
                    case 1:
                        params.gravity = Gravity.TOP | Gravity.CENTER;
                        break;
                    case 2:
                        params.gravity = Gravity.TOP | Gravity.LEFT;
                        break;
                    case 3:
                        params.gravity = Gravity.TOP | Gravity.RIGHT;
                        break;
                    case 4:
                        params.gravity = Gravity.CENTER | Gravity.CENTER;
                        break;
                    case 5:
                        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                        break;
                    case 6:
                        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                        break;
                    case 7:
                        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                        break;
                    default:
                        params.gravity = Gravity.TOP | Gravity.CENTER;
                }
                Intent sharingIntent = new Intent(Intent.ACTION_VIEW);
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                params.x = Integer.parseInt(position_x);
                params.y = Integer.parseInt(position_y);
                params.width = Integer.parseInt(width);
                params.height = Integer.parseInt(height);
                Log.i("width", String.valueOf(dpToPx(Integer.parseInt(width))));
                Log.i("height",String.valueOf(dpToPx(Integer.parseInt(height))));
                mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                mWindowManager.addView(mOverlayView, params);
                Display display = mWindowManager.getDefaultDisplay();

            } else {
                final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT);
                switch (Integer.parseInt(type_ads)) {
                    case 1:
                        params.gravity = Gravity.TOP | Gravity.CENTER;
                        break;
                    case 2:
                        params.gravity = Gravity.TOP | Gravity.LEFT;
                        break;
                    case 3:
                        params.gravity = Gravity.TOP | Gravity.RIGHT;
                        break;
                    case 4:
                        params.gravity = Gravity.CENTER | Gravity.CENTER;
                        break;
                    case 5:
                        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
                        break;
                    case 6:
                        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                        break;
                    case 7:
                        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                        break;
                    default:
                        params.gravity = Gravity.TOP | Gravity.CENTER;
                }
                Intent sharingIntent = new Intent(Intent.ACTION_VIEW);
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                params.x = Integer.parseInt(position_x);
                params.y = Integer.parseInt(position_y);
                params.width = Integer.parseInt(width);

                Log.i("width", String.valueOf(dpToPx(Integer.parseInt(width))));
                Log.i("height",String.valueOf(dpToPx(Integer.parseInt(height))));

                params.height = Integer.parseInt(height);
                mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                mWindowManager.addView(mOverlayView, params);
                Display display = mWindowManager.getDefaultDisplay();
                final Point size = new Point();
                display.getSize(size);
                timeShowAds=Long.parseLong(time_show);;
                String filename = "utivi_time.json";
            }

            WindowManager windowmanager = (WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            windowmanager.getDefaultDisplay().getMetrics(displayMetrics);
            if(Integer.parseInt(status)==1) {
               if(Integer.parseInt(type_ads)==4){
                    Uri videoUri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.ureka2);
                    content_url=String.valueOf(videoUri);
                    mVideoPlayer.setVideoPath(content_url);
                    mVideoPlayer.start();
               }else{
                    videoRootView.setVisibility(View.GONE);
                    CallWebView(String.valueOf(time_show), Integer.parseInt(position),  Integer.parseInt(width),  Integer.parseInt(height));

                }
            }
        }

    }
    /**
     * Checks if the device has any active internet connection.
     *
     * @return true device with internet connection, otherwise false.
     */
    public boolean isThereInternetConnection() {
        boolean isConnected;
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = (networkInfo != null && networkInfo.isConnectedOrConnecting());
        return isConnected;
    }
    class MyThread extends Thread{
        @Override
        public void run() {
            while (isRunning) {
                try {
                    if (isThereInternetConnection()) {
                        if (mOverlayView != null) {
                            mWindowManager.removeView(mOverlayView);
                        }
                        Log.i("URL",urlJson);
                        new GetJSONTask().execute(urlJson);
                        Utils.sendPostDevice(macId, serialId, location, "1");
                    }
                    Thread.sleep(timeShowAds);
                } catch (InterruptedException e) {
                    isRunning = false;
                }
            }
        }

    }
    /**
     * Request video ads from the given VAST ad tag.
     * @param adTagUrl URL of the ad's VAST XML
     */
}
