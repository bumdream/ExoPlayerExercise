package exoplayer.bumbums.exoplayerex;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

/**
 * Created by hanseungbeom on 2018. 4. 16..
 */

public class GifExtractor {
    private SimpleExoPlayer mExoPlayer;
    private BandwidthMeter mBandwidthMeter;
    private TrackSelection.Factory mVideoTrackSelectionFactory;
    private TrackSelector mTrackSelector;

    public GifExtractor(SimpleExoPlayer exoPlayer){
        mExoPlayer = exoPlayer;

        mBandwidthMeter = new DefaultBandwidthMeter();
        mVideoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(mBandwidthMeter);
        mTrackSelector = new DefaultTrackSelector(mVideoTrackSelectionFactory);

        ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        mExtractView.setPlayer(mExtractPlayer);
        mExtractPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        mExtractPlayer.setVideoTextureView(mTextureView);
    }

}
