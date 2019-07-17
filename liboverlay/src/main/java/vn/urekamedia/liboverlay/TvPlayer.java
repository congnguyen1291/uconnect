package vn.urekamedia.liboverlay;

import android.media.PlaybackParams;
import android.view.Surface;

/**
 * The TvPlayer contains common media controller methods that a player can use to interface with
 * the system TV app to handle user input events.
 */
public interface TvPlayer {
    /**
     * Interface for alerting caller of major video events.
     */
    public static abstract class Callback {
        /**
         * Called when the current video starts playing.
         */
        public void onStarted() {

        }

        /**
         * Called when the current video has completed playback to the end of the video.
         */
        public void onCompleted() {

        }

        /**
         * Called when an error occurs during video playback.
         */
        public void onError() {

        }

        /**
         * Called when the video is paused.
         */
        public void onPaused() {

        }

        /**
         * Called when the video is resumed.
         */
        public void onResumed() {

        }
    }

    /**
     * Sets the current position for the current media.
     *
     * @param position The current time in milliseconds to play the media.
     */
    void seekTo(long position);

    /**
     * Sets the playback params for the current media.
     *
     * @param params The new playback params.
     */
    void setPlaybackParams(PlaybackParams params);

    /**
     * @return The current time in milliseconds of the media.
     */
    long getCurrentPosition();

    /**
     * @return The total length of the currently loaded video in milliseconds.
     */
    long getDuration();

    /**
     * Sets the surface for the current media.
     *
     * @param surface The surface to play media on
     */
    void setSurface(Surface surface);

    /**
     * Sets the volume for the current media.
     *
     * @param volume The volume between 0 and 1 to play the media at.
     */
    void setVolume(float volume);

    /**
     * Pause the current media.
     */
    void pause();

    /**
     * Start playing or resume the current media.
     */
    void play();

    /**
     * Provide the player with a callback for major video events (started, completed, error, paused
     * and resumed).
     */
    void registerCallback(Callback callback);

    /**
     * Remove a player callback from getting notified on video events.
     */
    void unregisterCallback(Callback callback);
}