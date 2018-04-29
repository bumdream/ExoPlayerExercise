package exoplayer.bumbums.exoplayerex;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.google.android.exoplayer2.C.msToUs;

/**
 * Created by hanseungbeom on 2018. 4. 27..
 */

public class Utils {

    /* get ClippingMediaSource from startPos to endPos */
    public static MediaSource getExtractedVideo(MediaSource source, long startPos, long endPos) {
        return new ClippingMediaSource(source, msToUs(startPos), msToUs(endPos));
    }

    /* get default trackSelector */
    public static TrackSelector getDefaultTrackSelector(){
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        return trackSelector;
    }

    /* refresh gallery */
    public static void addImageToGallery(Context context, Uri gifUri) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/gif");
        values.put(MediaStore.MediaColumns.DATA, gifUri.getPath());
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /* gif sharing */
    public static void shareGif(Context context, Uri gifUri) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/gif");
        shareIntent.putExtra(Intent.EXTRA_STREAM, gifUri);
        context.startActivity(Intent.createChooser(shareIntent, "Share image"));
    }

    /* save gif to file and return uri */
    public static Uri makeGifFile(ByteArrayOutputStream baos) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/gif");
        if (!dir.exists())
            dir.mkdir();
        File outFile = new File(dir, "output.gif");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outFile);
            // Put data in your baos
            baos.writeTo(fos);
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

    public static int getSecFromMs(long milliseconds){
        return (int) (milliseconds / 1000) % 60 ;
    }

    public static int getDelayOfFrame(int fps){
       return (int) (((double) 1 / fps) * 1000);
    }

}
