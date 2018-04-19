package exoplayer.bumbums.exoplayerex;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.TextureView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.video.VideoListener;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Handler;

/**
 * Created by hanseungbeom on 2018. 4. 19..
 */

public class GifExtractor implements VideoListener, Player.EventListener {
    private Context mContext;
    private Hanlder mHandler;
    private SimpleExoPlayer mExtractPlayer;
    private PlayerView mPlayerView;
    private TextureView mTextureView;

    private GifEncoder mGifEncoder;
    private Queue<Long> mFramePos;
    private ByteArrayOutputStream mBaos;
    private int mFps;
    private boolean isInitialize;

    private boolean mDuration = false;

    public interface Hanlder {
        void onExtractionFinished(Uri gifUri);

        void onFrameExtracted(Bitmap frame);
    }

    public GifExtractor(Context context,PlayerView playerView, TextureView textureView) {
        mContext = context;
        mHandler = (Hanlder) context;
        mPlayerView = playerView;
        mTextureView = textureView;

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        mExtractPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        mPlayerView.setPlayer(mExtractPlayer);
        mExtractPlayer.setVideoTextureView(mTextureView);
        mExtractPlayer.getVideoComponent().addVideoListener(this);
        mExtractPlayer.addListener(this);

        mExtractPlayer.setPlayWhenReady(false);
        isInitialize = false;
    }


    public void run(MediaSource targetVideo) {
        mDuration = false;
        mExtractPlayer.prepare(targetVideo);
    }

    public void initEncoder() {
        int totalTimeMs = (int) mExtractPlayer.getDuration();
        double totalTimeSec = (float) totalTimeMs / 1000;
        //calculate total count of frames we need and delay between frames.
        int neededFrame = (int) (mFps * (totalTimeSec));
        int delayOfFrame = (int) (((double) 1 / mFps) * 1000);

        mFramePos = new LinkedList<>();

        //calculate frame Position needed
        for (long pos = 0; pos < totalTimeMs-100; pos += totalTimeMs / neededFrame) {
            mFramePos.add(pos);
        }

        mGifEncoder = new GifEncoder();
        mBaos = new ByteArrayOutputStream();
        mGifEncoder.start(mBaos);
        mGifEncoder.setDelay(delayOfFrame);
        isInitialize = true;
    }

    public void startExtract() {
        continueExtraction();
    }

    public void continueExtraction( ) {
        if (!mFramePos.isEmpty()) {
            long nextPos = mFramePos.poll();
            if(nextPos==0) {
                //when pos == 0 seekTo() method dones't call renderFirstFrame() .
                mExtractPlayer.seekTo(1);
            }
            else {
                mExtractPlayer.seekTo(nextPos);
            }
        }
    }

    public Bitmap getCurrentFrame() {
        return mTextureView.getBitmap();
    }

    public Uri getGifUri() {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/gif");
        if (!dir.exists())
            dir.mkdir();
        File outFile = new File(dir, "output.gif");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            // Put data in your baos
            mBaos.writeTo(fos);
        } catch (IOException ioe) {
            // Handle exception here
            ioe.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return Uri.fromFile(outFile);
    }

    public void setFps(int fps) {
        mFps = fps;
    }


    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    @Override
    public void onRenderedFirstFrame() {
        if (isInitialize) {
            if (!mFramePos.isEmpty()) {
                Bitmap frame = getCurrentFrame();
                mGifEncoder.addFrame(frame);
                mHandler.onFrameExtracted(frame);
                continueExtraction();
            }
            else{
                mGifEncoder.finish();
                mHandler.onExtractionFinished(getGifUri());
            }
        }

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        // when prepare finished come here!
        if (playbackState == Player.STATE_READY && !mDuration) {
            mDuration = true;
            initEncoder();
            startExtract();
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {
    }

    public void release(){
        mExtractPlayer.release();
    }


}
