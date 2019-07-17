package vn.urekamedia.liboverlay;

import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Manages Google Cast interactions.
 */
public class CastApplication implements Cast.MessageReceivedCallback,
        RemoteMediaClient.ProgressListener {

    private CastSession mCastSession;
    private SessionManager mSessionManager;
    private SessionManagerListener<CastSession> mSessionManagerListener;

    private double mPosition; // content time in secs.

    private static final String NAMESPACE = "urn:x-cast:com.google.ads.interactivemedia.dai.cast";
    private static final String TAG = "IMA Cast Example";

    private TextView mTimeStart;
    private TextView mTimeEnd;



    public void onResume() {
        mCastSession = mSessionManager.getCurrentCastSession();
        mSessionManager.addSessionManagerListener(mSessionManagerListener, CastSession.class);
    }

    public void onPause() {
        mSessionManager.removeSessionManagerListener(mSessionManagerListener, CastSession.class);
        mCastSession = null;
    }


    public void loadMedia(long position) throws JSONException {

        JSONObject streamRequest = new JSONObject();
        /*
        streamRequest.put("assetKey", mVideoListItem.getAssetKey());
        streamRequest.put("contentSourceId", mVideoListItem.getContentSourceId());
        streamRequest.put("videoId", mVideoListItem.getVideoId());
        streamRequest.put("apiKey", mVideoListItem.getApiKey());
        // turn off pre-roll for LIVE.
        streamRequest.put("attemptPreroll", mVideoListItem.isVod()); */
        streamRequest.put("startTime", position);

        MediaMetadata mediaMetadata = new MediaMetadata();
        mediaMetadata.addImage(new WebImage(
                Uri.parse("https://www.example.com")));


        MediaInfo mediaInfo = new MediaInfo.Builder("https://www.example.com")
                .setCustomData(streamRequest)
                .setMetadata(mediaMetadata)
                .setContentType("application/x-mpegurl")
                .build();

        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        remoteMediaClient.addProgressListener(this, 1000); // 1 sec period.
        remoteMediaClient.load(mediaInfo, true, 0);

    }

    @Override
    public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
        Log.d(TAG, "onMessageReceived: " + message);
        if (message.startsWith("contentTime,")) {
            String timeString = message.substring("contentTime,".length());
            try {
                mPosition = Double.valueOf(timeString);
            } catch (NumberFormatException e) {
                Log.e(TAG, "can't parse content time" + e);
            }
        }
    }

    private void sendMessage(String message) {
        try {
            mCastSession.sendMessage(NAMESPACE, message);
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message", e);
        }
    }

    @Override
    public void onProgressUpdated(long progressMs, long durationMs) {
        if (mCastSession == null) {
            return;
        }

        sendMessage("getContentTime,");
    }

    private String millisToTimeString(long millis) {
        return new SimpleDateFormat("mm:ss", Locale.US).format(new Date(millis));
    }
}
