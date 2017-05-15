package com.fycoder.fy.beauty.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fycoder.fy.beauty.R;
import com.fycoder.fy.beauty.view.ImageEditBeautyView;
import com.fycoder.fy.beauty.display.MagicImageDisplay;
import com.fycoder.fy.beauty.utils.Constants;
import com.fycoder.fy.beauty.utils.PermissionCheck;
import com.fycoder.fy.beauty.utils.SaveTask;

import java.io.InputStream;

/**
 * Created by fy on 2017/3/8.
 */

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";
    
    private MagicImageDisplay mMagicImageDisplay;
    private ImageEditBeautyView mImageEditBeautyView;

    private final int REQUEST_PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        //6.0系统权限检查
        PermissionCheck.verifyStoragePermissions(this);
        initMagicPreview();
        findViewById(R.id.text_begin).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mImageEditBeautyView = new ImageEditBeautyView(ImageActivity.this, mMagicImageDisplay);
                getFragmentManager().beginTransaction().add(R.id.image_edit_fragment_container, mImageEditBeautyView)
                        .show(mImageEditBeautyView).commit();

            }
        });
        findViewById(R.id.text_exit_save).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mMagicImageDisplay.savaImage(Constants.getOutputMediaFile(), new SaveTask.onPictureSaveListener() {

                    @Override
                    public void onSaved(String result) {
//                        Intent intent = new Intent();
//                        intent.setClass(ImageActivity.this, ShareActivity.class);
//                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "保存图片成功，图片存储位置：" + result, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mMagicImageDisplay != null)
            mMagicImageDisplay.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mMagicImageDisplay != null)
            mMagicImageDisplay.onResume();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(mMagicImageDisplay != null)
            mMagicImageDisplay.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri mUri = data.getData();
                        InputStream inputStream = ImageActivity.
                                this.getContentResolver().openInputStream(mUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        mMagicImageDisplay.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    finish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void initMagicPreview() {
        GLSurfaceView glSurfaceView = (GLSurfaceView)findViewById(R.id.glsurfaceview_image);
        mMagicImageDisplay = new MagicImageDisplay(this, glSurfaceView);

        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.fromFile(Constants.getOutputMdeiaDir());
        Log.d(TAG, "initMagicPreview: 获取图片的路径"+uri.toString());
        photoPickerIntent.setDataAndType(uri, "image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
    }
}
