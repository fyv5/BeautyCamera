package com.fycoder.fy.beauty.view;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.fycoder.fy.beauty.R;
import com.fycoder.fy.beauty.widget.BubbleSeekBar;
import com.fycoder.fy.beauty.display.MagicImageDisplay;
import com.fycoder.fy.beauty.utils.MagicSDK;

/**
 * Created by fy on 2017/3/9.
 */

public class ImageEditBeautyView extends ImageEditFragment {

    private RadioGroup mRadioGroup;
    private MagicSDK mMagicSDK;
    private RelativeLayout mSkinSmoothView;
    private RelativeLayout mSkinColorView;
    private BubbleSeekBar mSmoothBubbleSeekBar;
    private BubbleSeekBar mWhiteBubbleSeekBar;
    private boolean mIsSmoothed = false;
    private boolean mIsWhiten = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_edit_beauty, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mSkinSmoothView = (RelativeLayout) getView().findViewById(R.id.fragment_beauty_skin);
        mSkinSmoothView = (RelativeLayout) getView().findViewById(R.id.fragment_beauty_skin);
        mSkinColorView = (RelativeLayout) getView().findViewById(R.id.fragment_beauty_color);
        mRadioGroup = (RadioGroup)getView().findViewById(R.id.fragment_beauty_radiogroup);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.fragment_beauty_btn_skinsmooth:
                        mSkinSmoothView.setVisibility(View.VISIBLE);
                        mSkinColorView.setVisibility(View.GONE);
                        break;
                    case R.id.fragment_beauty_btn_skincolor:
                        mSkinColorView.setVisibility(View.VISIBLE);
                        mSkinSmoothView.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        });
        mMagicSDK = MagicSDK.getInstance();
        mMagicSDK.setMagicSDKListener(mMagicSDKListener);
        mSmoothBubbleSeekBar = (BubbleSeekBar) view.findViewById(R.id.fragment_beauty_skin_seekbar);
        mSmoothBubbleSeekBar.setOnBubbleSeekBarChangeListener(mOnSmoothBubbleSeekBarChangeListener);
        mWhiteBubbleSeekBar = (BubbleSeekBar) view.findViewById(R.id.fragment_beauty_white_seekbar);
        mWhiteBubbleSeekBar.setOnBubbleSeekBarChangeListener(mOnColorBubbleSeekBarChangeListener);
        init();
        super.onViewCreated(view, savedInstanceState);
    }

    private void init(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                mMagicSDK.initMagicBeauty();
            }
        }).start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            mSmoothBubbleSeekBar.setProgress(0);
            mWhiteBubbleSeekBar.setProgress(0);
            init();
        }else{
            mMagicSDK.uninitMagicBeauty();
        }
    }

    private BubbleSeekBar.OnBubbleSeekBarChangeListener mOnSmoothBubbleSeekBarChangeListener =
            new BubbleSeekBar.OnBubbleSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    float level = seekBar.getProgress() / 10.0f;
                    if(level < 0)
                        level = 0;
                    mMagicSDK.onStartSkinSmooth(level);
                    if(seekBar.getProgress() != 0)
                        mIsSmoothed = true;
                    else
                        mIsSmoothed = false;
                }
            }).start();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }
    };

    private BubbleSeekBar.OnBubbleSeekBarChangeListener mOnColorBubbleSeekBarChangeListener =
            new BubbleSeekBar.OnBubbleSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    float level = seekBar.getProgress() / 20.0f;
                    if(level < 1)
                        level = 1;
                    mMagicSDK.onStartWhiteSkin(level);
                    if(seekBar.getProgress() != 0)
                        mIsWhiten = true;
                    else
                        mIsWhiten = false;
                }
            }).start();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }
    };

    private MagicSDK.MagicSDKListener mMagicSDKListener = new MagicSDK.MagicSDKListener() {

        @Override
        public void onEnd() {

        }
    };

    public ImageEditBeautyView(Context context, MagicImageDisplay magicDisplay) {
        super(context, magicDisplay);
    }

    @Override
    protected boolean isChanged() {
        return mIsWhiten || mIsSmoothed;
    }

    @Override
    protected void onDialogButtonClick(DialogInterface dialog) {
        mMagicSDK.uninitMagicBeauty();
        super.onDialogButtonClick(dialog);
    }
}
