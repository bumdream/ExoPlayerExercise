package exoplayer.bumbums.exoplayerex;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import exoplayer.bumbums.exoplayerex.gif.GifEncoder;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "#####";

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1;

    private Button mPickBtn, mStartBtn, mEndBtn, mExtractBtn;
    private EditText mEtStart, mEtEnd, mEtFps;
    private ImageView mFrameView;
    private PlayerView mPlayerView, mExtractView;
    private SimpleExoPlayer mPlayer, mExtractPlayer;
    private TextureView mTextureView;
    private GifImageView mGifView;
    private boolean mDuration;

    private MediaSource mOrigin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mPickBtn = (Button) findViewById(R.id.btn_pick);
        mPickBtn.setOnClickListener(this);

        mStartBtn = (Button) findViewById(R.id.btn_start);
        mStartBtn.setOnClickListener(this);

        mEndBtn = (Button) findViewById(R.id.btn_end);
        mEndBtn.setOnClickListener(this);

        mExtractBtn = (Button) findViewById(R.id.btn_get_gif);
        mExtractBtn.setOnClickListener(this);

        mEtStart = (EditText) findViewById(R.id.et_start);
        mEtEnd = (EditText) findViewById(R.id.et_end);
        mEtFps = (EditText) findViewById(R.id.et_fps);

        mPlayerView = (PlayerView) findViewById(R.id.ev);
        mExtractView = (PlayerView) findViewById(R.id.ev_extract_view);
        mFrameView = (ImageView) findViewById(R.id.iv_frame);
        mTextureView = (TextureView) findViewById(R.id.tv);

        mGifView = (GifImageView)findViewById(R.id.gif_view);

        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the mPlayer
        mPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        mPlayerView.setPlayer(mPlayer);

/*        RenderersFactory renderersFactory = new DefaultRenderersFactory(this);
        DefaultAllocator allocator = new DefaultAllocator(true,1,1);
        LoadControl loadControl = new DefaultLoadControl(
                allocator,
                5000,
                5000,
                5000,
                5000,
                1000,
                false);

        mExtractPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector,loadControl);
        */
        mExtractPlayer = ExoPlayerFactory.newSimpleInstance(this,trackSelector);
        mExtractView.setPlayer(mExtractPlayer);
        mExtractPlayer.setVideoTextureView(mTextureView);
        mExtractPlayer.addListener(eventListener);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_pick:
                //Pick video from internal storage

                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
                break;
            case R.id.btn_start:
                mEtStart.setText(String.valueOf(mPlayer.getCurrentPosition()));
                break;
            case R.id.btn_end:
                mEtEnd.setText(String.valueOf(mPlayer.getCurrentPosition()));
                break;
            case R.id.btn_get_gif:
                if (mOrigin != null) {
                    //it is for duration sync
                    mDuration = false;

                    long startPos = Long.parseLong(mEtStart.getText().toString());
                    long endPos = Long.parseLong(mEtEnd.getText().toString());
                    ClippingMediaSource extractedSource = new ClippingMediaSource(mOrigin, startPos * 1000, endPos * 1000);
                    mExtractPlayer.prepare(extractedSource);

                    //if prepare finish , function makeGIF() will run.
                } else {
                    Toast.makeText(this, "no video", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    public void makeGIF() {
        long totalTimeMs = mExtractPlayer.getDuration();
        double totalTimeSec = (float) totalTimeMs / 1000;
        //calculate total count of frames we need and delay between frames.
        int fps = Integer.parseInt(mEtFps.getText().toString());
        int neededFrame = (int) (fps * (totalTimeSec));
        final int delayOfFrame = (int) (((double) 1 / fps)* 1000) ;

        final ArrayList<Long> framePos = new ArrayList<>();

        //add framePos
        for (long pos = 0; pos < totalTimeMs; pos += totalTimeMs / neededFrame) {
            framePos.add(pos);
        }

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File outFile = new File(MainActivity.this.getFilesDir(),"output.gif");
                    //GIF Encoder start
                    GifEncoder gifEncoder = new GifEncoder();
                    gifEncoder.start(outFile.getCanonicalPath());
                    gifEncoder.setDelay(delayOfFrame);

                    for (int i = 0; i < framePos.size(); i++) {
                        long pos = framePos.get(i);
                        mExtractPlayer.seekTo(pos);

                        //TODO seekTo is async so have to check it
                        final Bitmap bitmap = mTextureView.getBitmap();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mFrameView.setImageBitmap(bitmap);
                            }
                        });
                        gifEncoder.addFrame(bitmap);
                    }


                    // Make the gif
                    gifEncoder.finish();

                    // Add to gallery
                    Uri picUri = Uri.fromFile(outFile);

                    setGif(picUri);

                } catch (IOException err) {

                }
            }
        });
       t.start();


    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedVideoUri = data.getData();

                if (selectedVideoUri != null) {
                    setVideo(selectedVideoUri);
                }
            }
        }
    }

    public void setVideo(Uri videoUri) {
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter1 = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter1);
        // This is the MediaSource representing the media to be played.
        mOrigin = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(videoUri);

        // Prepare the mPlayer with the source.
        mPlayer.prepare(mOrigin);
        // mPlayer.setPlayWhenReady(true); //run file/link when ready to play.
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
        mPlayer.release();
        mExtractPlayer.release();
    }

    Player.EventListener eventListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            Log.v(TAG, "onTimelineChanged:" + timeline.toString());
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.v(TAG, "onLoadingChanged:" + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            // when prepare finished come here!
            if (playbackState == Player.STATE_READY && !mDuration) {
                mDuration = true;
                makeGIF();
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
            Log.v(TAG, "onPlaybackParametersChanged:" + playbackParameters.toString());

        }

        @Override
        public void onSeekProcessed() {
            Log.v(TAG, "onSeekProcessed:" + mExtractPlayer.getCurrentPosition());
        }
    };

    public void setFrame(final Bitmap bitmap){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrameView.setImageBitmap(bitmap);
            }
        });
    }
    public void setGif(final Uri gifUri){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    GifDrawable gifUriDrawable = new GifDrawable(null, gifUri);
                    gifUriDrawable.setLoopCount(0);
                    mGifView.setImageDrawable(gifUriDrawable);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
