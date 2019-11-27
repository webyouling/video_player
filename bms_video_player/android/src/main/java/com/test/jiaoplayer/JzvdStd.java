package com.test.jiaoplayer;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.test.bms_video_player.R;

public class JzvdStd extends Jzvd{
    public JzvdStd(@NonNull Context context) {
        super(context);
    }

    public JzvdStd(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public JzvdStd(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.jz_layout_std;
    }
}
