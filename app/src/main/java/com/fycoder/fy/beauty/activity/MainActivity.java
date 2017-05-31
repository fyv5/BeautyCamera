package com.fycoder.fy.beauty.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fycoder.fy.beauty.R;
import com.fycoder.fy.beauty.fragment.CameraFragment;
import com.fycoder.fy.beauty.utils.ActivityUtil;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CameraFragment cameraFragment =
                (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (cameraFragment == null) {
            cameraFragment = CameraFragment.newInstance();

            ActivityUtil.addFragmentToActivity(getSupportFragmentManager(),
                    cameraFragment,
                    R.id.fragment_container);
        }
    }
}
