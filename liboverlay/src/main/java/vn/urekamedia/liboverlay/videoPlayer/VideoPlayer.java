// Copyright 2014 Google Inc. All Rights Reserved.

package vn.urekamedia.liboverlay.videoPlayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

import java.util.ArrayList;
import java.util.List;

import static vn.urekamedia.liboverlay.FloatingWidgetService.AD_UNIT_ID_VIDEO;
import static vn.urekamedia.liboverlay.FloatingWidgetService.content_url;
import static vn.urekamedia.liboverlay.FloatingWidgetService.mAdUiContainer;
import static vn.urekamedia.liboverlay.FloatingWidgetService.mVideoPlayer;


/**
 * A VideoView that intercepts various methods and reports them back via a
 * OnVideoCompletedListener.
 */

public class VideoPlayer extends VideoView implements AdEvent.AdEventListener, AdErrorEvent.AdErrorListener {
    private static String LOGTAG = "Ureka Ads";

    // Whether an ad is displayed.
    public static boolean mIsAdDisplayed;

    // The play button to trigger the ad request.
    private View mPlayButton;
    private boolean isFullScreen;
    View view;
    public static ImaSdkFactory mSdkFactory;
    public static AdsLoader mAdsLoader;

    public static AdsManager mAdsManager;
    public static View VideoLayout;
    /**
     * Interface for alerting caller of video completion.
     */
    public interface OnVideoCompletedListener {
        /**
         * Called when the current video has completed playback to the end of the video.
         */
        void onVideoCompleted();

    }

    private final List<OnVideoCompletedListener> mOnVideoCompletedListeners = new ArrayList<>(1);

    public VideoPlayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressLint("CutPasteId")
    public VideoPlayer(Context context) {
        super(context);
        init();
    }

    /**
     * Request video ads from the given VAST ad tag.
     * @param adTagUrl URL of the ad's VAST XML
     */
    public static void requestAds(String adTagUrl) {
        AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(mAdUiContainer);
        Log.i("Tag",adTagUrl);
        // Create the ads request.
        AdsRequest request = mSdkFactory.createAdsRequest();
        request.setVastLoadTimeout(100000);
        request.setAdTagUrl(adTagUrl);
        request.setAdDisplayContainer(adDisplayContainer);
        request.setContentProgressProvider(new ContentProgressProvider() {
            @Override
            public VideoProgressUpdate getContentProgress() {
                if (mIsAdDisplayed || mVideoPlayer == null || mVideoPlayer.getDuration() <= 0) {
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new VideoProgressUpdate(mVideoPlayer.getCurrentPosition(),
                        mVideoPlayer.getDuration());
            }
        });

        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader.requestAds(request);
    }

    public void init() {
        // Create an AdsLoader.
        MediaController mediaController = new MediaController(getContext());
        mediaController.setAnchorView(this);

        // Set OnCompletionListener to notify our listeners when the video is completed.
        super.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mVideoPlayer.pause();
                // Reset the MediaPlayer.
                mVideoPlayer.addVideoCompletedListener(new VideoPlayer.OnVideoCompletedListener() {
                    @Override
                    public void onVideoCompleted() {
                        // Handle completed event for playing post-rolls.
                        if (mAdsLoader != null) {
                            mAdsLoader.contentComplete();
                        }
                    }
                });
                mediaPlayer.reset();
                mediaPlayer.setDisplay(getHolder());
                // Create an AdsLoader.
                mSdkFactory = ImaSdkFactory.getInstance();
                AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
                adDisplayContainer.setAdContainer(mAdUiContainer);
                ImaSdkSettings settings = mSdkFactory.createImaSdkSettings();
                mAdsLoader = mSdkFactory.createAdsLoader(
                        mVideoPlayer.getContext(), settings, adDisplayContainer);
                mAdsLoader.addAdErrorListener(mVideoPlayer);
                mAdsLoader.addAdsLoadedListener(new AdsLoader.AdsLoadedListener() {
                    @Override
                    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                        mAdsManager = adsManagerLoadedEvent.getAdsManager();
                        mAdsManager.addAdErrorListener(mVideoPlayer);
                        mAdsManager.addAdEventListener(mVideoPlayer);
                        mAdsManager.init();
                    }
                });
                mVideoPlayer.setVideoPath(content_url);
                requestAds(AD_UNIT_ID_VIDEO);
                for (OnVideoCompletedListener listener : mOnVideoCompletedListeners) {
                    listener.onVideoCompleted();
                }

            }
        });

        // Set OnErrorListener to notify our listeners if the video errors.
        super.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // Returning true signals to MediaPlayer that we handled the error. This will
                // prevent the completion handler from being called.
                return true;
            }
        });
    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        switch (adEvent.getType()) {
            case LOADED:
                mAdsManager.start();
                mVideoPlayer.pause();
                break;
            case CONTENT_PAUSE_REQUESTED:
                mIsAdDisplayed = true;
                mVideoPlayer.pause();
                break;
            case CONTENT_RESUME_REQUESTED:
                mIsAdDisplayed = false;
                mVideoPlayer.play();
                break;
            case ALL_ADS_COMPLETED:
                if (mAdsManager != null) {
                    mAdsManager.destroy();
                    mAdsManager = null;
                }
                mVideoPlayer.play();
                break;
            default:
                break;
        }

    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        Log.e(LOGTAG, "Ad Error: " + adErrorEvent.getError().getMessage());
        mVideoPlayer.play();
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        Log.i("Video start 1","Runing");
        play();
        throw new UnsupportedOperationException();
    }

    public void play() {
        Log.i("Video start 2","Runing");
        start();
    }

    public void addVideoCompletedListener(OnVideoCompletedListener listener) {
      //  mAdsManager.start();
        mOnVideoCompletedListeners.add(listener);
    }

}
