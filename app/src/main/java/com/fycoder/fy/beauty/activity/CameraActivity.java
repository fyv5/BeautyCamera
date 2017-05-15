package com.fycoder.fy.beauty.activity;

import android.app.Activity;
import android.os.Bundle;

import com.fycoder.fy.beauty.R;
import com.fycoder.fy.beauty.camera.Camera2BasicFragment;
import com.fycoder.fy.beauty.utils.PermissionCheck;

/**
 * Created by fy on 2017/3/10.
 */

public class CameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1);
        PermissionCheck.verifyStoragePermissions(this);
        if (null == savedInstanceState)  {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }
}
