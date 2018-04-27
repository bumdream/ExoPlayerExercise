package exoplayer.bumbums.exoplayerex;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.TextureView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.video.VideoListener;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hanseungbeom on 2018. 4. 19..
 */

public class GifExtractor implements VideoListener, Player.EventListener {

    //callback
    private Hanlder mHandler;

    //about exoplayer
    private SimpleExoPlayer mExtractPlayer;
    private PlayerView mPlayerView;
    private TextureView mTextureView;

    //about gif encoder
    private GifEncoder mGifEncoder;
    private Queue<Long> mFramePos;

    //about making gif to file.
    private ByteArrayOutputStream mBaos;

    //fps
    private int mFps;

    //this flag for checking if extractor init or not
    private boolean isInitialize;

    //this flag for checking if video loaded or not
    private boolean mDuration;

    /* interface */
    public interface Hanlder {
        /* This method is called after creating gif file. */
        void onExtractionFinished(Uri gifUri);
        /* This method is called whenever extract frame from video */
        void onFrameExtracted(Bitmap frame);
    }

    /* constructor */
    public GifExtractor(Context context,PlayerView playerView, TextureView textureView) {
        mHandler = (Hanlder) context;

        /* exoplayer setting */
        mPlayerView = playerView;
        mTextureView = textureView;
        mExtractPlayer = ExoPlayerFactory.newSimpleInstance(context, Utils.getDefaultTrackSelector());
        mPlayerView.setPlayer(mExtractPlayer);
        mExtractPlayer.setVideoTextureView(mTextureView);
        mExtractPlayer.getVideoComponent().addVideoListener(this);
        mExtractPlayer.addListener(this);
        mExtractPlayer.setPlayWhenReady(false);

        isInitialize = false;
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
    
    public void run(MediaSource targetVideo) {
        mDuration = false;
        mExtractPlayer.prepare(targetVideo);
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

    /* this method is called when exoplayer view changed */
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
                mHandler.onExtractionFinished(Utils.makeGifFile(mBaos));
            }
        }

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        /* when prepare finished come here! */
        if (playbackState == Player.STATE_READY && !mDuration) {
            mDuration = true;
            initEncoder();
            startExtract();
        }
    }

    public void release(){
        mExtractPlayer.release();
    }

    public void setFps(int fps) {
        mFps = fps;
    }

    public Bitmap getCurrentFrame() {
        return mTextureView.getBitmap();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {}

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {}

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {}

    @Override
    public void onLoadingChanged(boolean isLoading) {}

    @Override
    public void onRepeatModeChanged(int repeatMode) {}

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {}

    @Override
    public void onPlayerError(ExoPlaybackException error) {}

    @Override
    public void onPositionDiscontinuity(int reason) {}

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {}

    @Override
    public void onSeekProcessed() {}


}
