package exoplayer.bumbums.exoplayerex;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by hanseungbeom on 2018. 4. 17..
 */


public class AppPermissions {
    public static final String[] APP_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static boolean hasAppPermission(Context context) {
        for (String permission : APP_PERMISSION) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}