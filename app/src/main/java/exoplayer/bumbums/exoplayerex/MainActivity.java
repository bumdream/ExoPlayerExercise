package exoplayer.bumbums.exoplayerex;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GifExtractor.Hanlder{
    private static final String TAG = "#####";

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1;

    private Button mPickBtn, mStartBtn, mEndBtn, mExtractBtn;
    private EditText mEtStart, mEtEnd, mEtFps;
    private ImageView mFrameView;
    private PlayerView mPlayerView, mExtractView;
    private SimpleExoPlayer mPlayer;
    private TextureView mTextureView;
    private GifImageView mGifView;
    private boolean mDuration;

    private MediaSource mOrigin;

    private GifExtractor mExtractor;

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

        mGifView = (GifImageView) findViewById(R.id.gif_view);

        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the mPlayer
        mPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        mPlayerView.setPlayer(mPlayer);
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
                    if(mExtractor!=null)
                        mExtractor.release();
                    mExtractor = new GifExtractor(this,mExtractView,mTextureView);
                    mExtractor.setFps(Integer.parseInt(mEtFps.getText().toString()));
                    mExtractor.run(getExtractedVideo());
                } else {
                    Toast.makeText(this, "no video", Toast.LENGTH_SHORT).show();
                }
                break;

        }
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
    }


    public void setFrame(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFrameView.setImageBitmap(bitmap);
            }
        });
    }

    public void setGif(final Uri gifUri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    GifDrawable gifUriDrawable = new GifDrawable(null, gifUri);
                    gifUriDrawable.setLoopCount(0);
                    mGifView.setImageDrawable(gifUriDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addImageToGallery(Uri gifUri) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/gif");
        values.put(MediaStore.MediaColumns.DATA, gifUri.getPath());

        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    }


    public void shareGif(Uri gifUri) {
        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/gif");
        shareIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
        startActivity(Intent.createChooser(shareIntent, "Share image"));
    }




    public MediaSource getExtractedVideo() {
        long startPos = Long.parseLong(mEtStart.getText().toString());
        long endPos = Long.parseLong(mEtEnd.getText().toString());
        ClippingMediaSource extractedSource = new ClippingMediaSource(mOrigin, startPos * 1000, endPos * 1000);
        return extractedSource;
    }

    @Override
    public void onExtractionFinished(Uri gifUri) {
        setGif(gifUri);
        addImageToGallery(gifUri);
        shareGif(gifUri);
    }

    @Override
    public void onFrameExtracted(Bitmap frame) {
        setFrame(frame);
    }


}
