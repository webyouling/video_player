package com.test.jiaoplayer;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.test.adapter.ClarityAdapter;
import com.test.adapter.SelectionsAdapter;
import com.test.bms_video_player.R;
import com.test.player.Loger;
import com.zx.flutter_download.DownloadModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import com.zx.flutter_download.DownloadModel;

import java.util.List;

import other.DownLoadBean;
import other.MovieHLS;
import tv.danmaku.ijk.media.player.IMediaPlayer;


public abstract class Jzvd extends FrameLayout implements View.OnClickListener, View.OnTouchListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "Jzvd";
    public static LinkedList<ViewGroup> CONTAINER_LIST = new LinkedList<>();//保存播放器容器
    public static int FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    /**
     * 播放错误
     **/
    public static final int STATE_ERROR = -1;
    /**
     * 播放未开始
     **/
    public static final int STATE_IDLE = 0;
    /**
     * 播放准备中
     **/
    public static final int STATE_PREPARING = 1;
    /**
     * 播放准备就绪
     **/
    public static final int STATE_PREPARED = 2;
    /**
     * 正在播放
     **/
    public static final int STATE_PLAYING = 3;
    /**
     * 暂停播放
     **/
    public static final int STATE_PAUSED = 4;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     **/
    public static final int STATE_BUFFERING_PLAYING = 5;
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
     **/
    public static final int STATE_BUFFERING_PAUSED = 6;
    /**
     * 播放完成
     **/
    public static final int STATE_COMPLETED = 7;

    /**
     * 普通模式
     **/
    public static final int MODE_NORMAL = 10;
    /**
     * 全屏模式
     **/
    public static final int MODE_FULL_SCREEN = 11;
    /**
     * 小窗口模式
     **/
    public static final int MODE_TINY_WINDOW = 12;

    private int mCurrentState = STATE_IDLE;//默认
    private int mCurrentMode = MODE_NORMAL;//默认竖屏播放

    public ViewGroup textureViewContainer;//播放器渲染容器
    public JZTextureView textureView;

    private LinearLayout mLoading;//加载动画
    private TextView mLoadText;//加载显示文本

    private LinearLayout mError;//播放错误
    private TextView mRetry;//点击重试

    private LinearLayout mTop;//顶部控制区
    private ImageView mBack;
    private TextView mTitle;//视频标题

    private RelativeLayout mDefaultMode;//默认模式
    private TextView mDefaultPosition;//视频播放当前时间
    private SeekBar mDefaultSeek;//视频播放进度条
    private TextView mDefaultTotalTime;//视频总时间
    private ImageView mFullScreen;//进入全屏
    private ImageView mDefaultPlay;//播放按钮
    private ImageView mDefaultVolume;//音量

    private LinearLayout mBottom;//全屏底部控制区
    private TextView mPosition;//视频播放当前时间
    private SeekBar mSeek;//视频播放进度条
    private TextView mTotalTime;//视频总时间
    private ImageView mShrinkScreen;//退出全屏
    private ImageView mPlayScreen;//播放按钮
    private TextView mSelections;//选集
    private TextView mClarity;//清晰度

    private RelativeLayout mFullCenter;//全屏中间布局
    private ImageView mLock;//屏幕锁
    private ImageView mPlay;//播放或暂停
    private LinearLayout mCopyAndVolume;
    private ImageView mCopy;//复制
    private ImageView mVolume;//音量

    private RelativeLayout gesture_volume_layout;// 音量控制布局
    private TextView geture_tv_volume_percentage;// 音量百分比
    private ProgressBar geture_tv_volume_percentage_progress;// 音量百分比
    private ImageView gesture_iv_player_volume;// 音量图标
    private RelativeLayout gesture_light_layout;// 亮度布局
    private TextView geture_tv_light_percentage;// 亮度百分比
    private ProgressBar geture_tv_light_percentageProgress;// 亮度百分比
    private RelativeLayout gesture_progress_layout;// 进度图标
    private TextView geture_tv_progress_time;// 播放时间进度
    private ImageView gesture_iv_progress;// 快进或快退标志

    private RelativeLayout video_condition_layout;//video列表
    private ListView condition_lv;
    private TextView emptyView;

    protected int mScreenWidth;
    protected int mScreenHeight;

    private boolean mIsLock = false;//是否锁频
    private boolean lockVisible;//底部是否显示
    private boolean topBottomVisible;//底部是否显示

    protected AudioManager mAudioManager;
    public Class mediaInterfaceClass = JZMediaIjk.class;//默认为ijk
    public JZMediaInterface mediaInterface;//播放器内核
    public static Jzvd CURRENT_JZVD;
    public JZDataSource jzDataSource;

    private DownLoadBean mCurrentVideo;//当前播放的视频
    private int mCurrentClarity = -1;//当前分辨率
    private ClarityAdapter mClarityAdapter;
    private SelectionsAdapter mSelectionsAdapter;
    private boolean isShowLayout = false;
    public int videoRotation = 0;

    public Jzvd(@NonNull Context context) {
        super(context);
        init(context);
    }

    public Jzvd(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Jzvd(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void unRequestOrientationEventListener() {
        if (mVolumeChangeObserver != null && getContext() != null) {
            getContext().unregisterReceiver(mVolumeChangeObserver);
        }
    }

    public void setDownLoadBean(DownLoadBean bean) {
        this.mCurrentVideo = bean;
    }

    public void setTitle(String title) {
        mTitle.setText(title);//设置视频标题
    }

    public void setResolution(String resolution) {//设置视频分辨率
        mCurrentClarity = Integer.parseInt(resolution);
        setTextViewClarity(mCurrentClarity);
    }

    public void setUp(String url, String title) {
        setUp(new JZDataSource(url, title), MODE_NORMAL);
    }

    public void setUp(String url, String title, int screen) {
        setUp(new JZDataSource(url, title), screen);
    }

    public void setUp(JZDataSource jzDataSource, int screen) {
        setUp(jzDataSource, screen, JZMediaIjk.class);
    }

    public void setUp(String url, String title, int screen, Class mediaInterfaceClass) {
        setUp(new JZDataSource(url, title), screen, mediaInterfaceClass);
    }

    public void setUp(JZDataSource jzDataSource, int screen, Class mediaInterfaceClass) {
        this.jzDataSource = jzDataSource;
        onStateNormal();
        this.mediaInterfaceClass = mediaInterfaceClass;
    }

    public void changeUrl(String url, String title, long seekToInAdvance) {
        mCurrentState = STATE_IDLE;
        this.skipToPosition = seekToInAdvance;
        this.jzDataSource = new JZDataSource(url, title);
        mediaInterface.setSurface(null);
        mediaInterface.release();
        startVideo();
    }

    public boolean getIsLock() {
        return mIsLock;
    }

    public int getCurrentClarity() {
        return mCurrentClarity;
    }

    private void init(Context context) {
        View.inflate(context, getLayoutId(), this);
        textureViewContainer = findViewById(R.id.surface_container);//播放渲染容器
        findLodingViwe();//加载动画
        findErrorViwe();//播放错误
        findTopView();//顶部控制区
        findNormalScreenView();//竖屏
        findFullScreenView();
        initGesture();
        textureViewContainer.setOnClickListener(this);
        textureViewContainer.setOnTouchListener(this);
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        initBoardCastReceiver();
    }

    private VolumeBroadcastReceiver mVolumeChangeObserver;
    private final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private int mVolumeChange = 0;

    private void initBoardCastReceiver() {
        mVolumeChangeObserver = new VolumeBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOLUME_CHANGED_ACTION);
        getContext().registerReceiver(mVolumeChangeObserver, filter);
    }

    private class VolumeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //媒体音量改变才通知
            if (VOLUME_CHANGED_ACTION.equals(intent.getAction())) {
                int volume = getVolume();//当前音量
                volumeChangeVolume(volume);
            }
        }
    }

    private void volumeChangeVolume(int volume) {
        if (volume != 0) {
            if (mVolumeChange == 0) {
                mDefaultVolume.setImageResource(R.mipmap.ic_round_volume_up);
                mVolume.setImageResource(R.mipmap.ic_round_volume_up);
                mVolumeChange = 1;
            }
        } else {
            mVolumeChange = 0;
            mDefaultVolume.setImageResource(R.mipmap.ic_round_volume_off);
            mVolume.setImageResource(R.mipmap.ic_round_volume_off);
        }
    }

    private void initGesture() {
        //音量的布局
        gesture_volume_layout = findViewById(R.id.mn_gesture_volume_layout);
        geture_tv_volume_percentage = findViewById(R.id.mn_gesture_tv_volume_percentage);
        geture_tv_volume_percentage_progress = findViewById(R.id.mn_gesture_tv_volume_percentage_progress);
        gesture_iv_player_volume = findViewById(R.id.mn_gesture_iv_player_volume);
        //进度的布局
        gesture_progress_layout = findViewById(R.id.mn_gesture_progress_layout);
        geture_tv_progress_time = findViewById(R.id.mn_gesture_tv_progress_time);
        gesture_iv_progress = findViewById(R.id.mn_gesture_iv_progress);

        //亮度的布局
        gesture_light_layout = findViewById(R.id.mn_gesture_light_layout);
        geture_tv_light_percentage = findViewById(R.id.mn_geture_tv_light_percentage);
        geture_tv_light_percentageProgress = findViewById(R.id.mn_geture_tv_light_percentage_progress);

        video_condition_layout = findViewById(R.id.popup_video_condition_layout);
        condition_lv = findViewById(R.id.popup_condition_lv);
        emptyView = findViewById(R.id.emptyView);
        hideVideoListViewLayout(false);
        hideAllGestureLayout();
    }


    private void findFullScreenView() {
        //全屏底部控制区
        mBottom = findViewById(R.id.full_bottom);
        //视频播放当前时间
        mPosition = findViewById(R.id.full_position);
        //视频播放进度条
        mSeek = findViewById(R.id.full_seek);
        //退出全屏
        mShrinkScreen = findViewById(R.id.full_bottom_exit);
        //视频总时间
        mTotalTime = findViewById(R.id.full_total_time);
        //底部播放
        mPlayScreen = findViewById(R.id.full_bottom_play);
        //选集
        mSelections = findViewById(R.id.full_selections);
        //清晰度
        mClarity = findViewById(R.id.full_clarity);
        //中部布局
        mFullCenter = findViewById(R.id.rl_full_center);
        //锁
        mLock = findViewById(R.id.full_lock);
        //播放或暂停
        mPlay = findViewById(R.id.full_play);
        //音量和复制
        mCopyAndVolume = findViewById(R.id.full_copy_volume);
        //复制
        mCopy = findViewById(R.id.full_copy);
        //音量
        mVolume = findViewById(R.id.full_volume);
        mSeek.setOnSeekBarChangeListener(this);
        mShrinkScreen.setOnClickListener(this);
        mPlayScreen.setOnClickListener(this);
        mSelections.setOnClickListener(this);
        mClarity.setOnClickListener(this);
        mLock.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        mCopyAndVolume.setOnClickListener(this);
        mCopy.setOnClickListener(this);
        mVolume.setOnClickListener(this);
    }

    private void findNormalScreenView() {
        //默认模式
        mDefaultMode = findViewById(R.id.rl_default_bottom);
        //视频播放当前时间
        mDefaultPosition = findViewById(R.id.default_position);
        //视频总时间
        mDefaultTotalTime = findViewById(R.id.default_total_time);
        //进入全屏
        mFullScreen = findViewById(R.id.default_bottom_fullscreen);
        //视频播放进度
        mDefaultSeek = findViewById(R.id.default_seek);
        //播放
        mDefaultPlay = findViewById(R.id.default_bottom_play);
        //音量
        mDefaultVolume = findViewById(R.id.default_volume);

        mFullScreen.setOnClickListener(this);
        mDefaultPlay.setOnClickListener(this);
        mDefaultSeek.setOnSeekBarChangeListener(this);
        mDefaultVolume.setOnClickListener(this);
    }

    private void findTopView() {
        //顶部控制区
        mTop = findViewById(R.id.top);
        //返回小窗口模式
        mBack = findViewById(R.id.back);
        //视频标题
        mTitle = findViewById(R.id.title);
        mBack.setOnClickListener(this);
    }

    private void findErrorViwe() {
        //播放错误
        mError = findViewById(R.id.error);
        //点击重试
        mRetry = findViewById(R.id.retry);
        mRetry.setOnClickListener(this);
    }

    private void findLodingViwe() {
        //加载动画
        mLoading = findViewById(R.id.loading);
        mLoadText = findViewById(R.id.load_text);
    }

    protected abstract int getLayoutId();

    public void startVideo(long position) {
        skipToPosition = position;
        startVideo();
    }

    public void startVideo() {//相当于之前的start()
        Log.d(TAG, "startVideo [" + this.hashCode() + "] ");
        setCurrentJzvd(this);
        try {
            Constructor<JZMediaInterface> constructor = mediaInterfaceClass.getConstructor(Jzvd.class);
            this.mediaInterface = constructor.newInstance(this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        addTextureView();

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        JZUtils.scanForActivity(getContext()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        mediaInterface.prepare();
        onStatePreparing();
    }

    public void onStatePreparing() {
        Log.i(TAG, "onStatePreparing " + " [" + this.hashCode() + "] ");
        mCurrentState = STATE_PREPARING;
        onPlayStateChanged(mCurrentState);
        resetProgressAndTime();
    }

    private void onPlayStateChanged(int state) {//状态改变
        switch (state) {
            case STATE_IDLE:
                //播放未开始
                break;
            case STATE_PREPARING:
                //播放准备中
                mLoading.setVisibility(View.VISIBLE);//加载布局
                mLoadText.setText(R.string.loading);
                mError.setVisibility(View.GONE);//加载错误布局
                mFullCenter.setVisibility(View.GONE);//全屏播放布局
                mTop.setVisibility(View.GONE);//顶部控制区布局
                mBottom.setVisibility(View.GONE);//全屏底部控制区布局
                mDefaultMode.setVisibility(View.GONE);//默认模式布局
                mDefaultVolume.setVisibility(View.GONE);//默认模式音量
                mPlay.setVisibility(View.GONE);
                break;
            case STATE_PREPARED:
                if (mSelectionController != null) {
                    mSelectionController.onVideoPrepared();
                }
                //播放准备就绪
                startUpdateProgressTimer();//开启进度更新
                break;
            case STATE_PLAYING:
                setTopBottomVisible(true);
                //开始播放
                mLoading.setVisibility(View.GONE);//隐藏加载布局
                //改变播放按钮图标
                mPlay.setVisibility(View.INVISIBLE);
                mPlay.setImageResource(R.mipmap.ic_round_pause_circle_outline);
                mDefaultPlay.setImageResource(R.mipmap.ic_round_pause);
                mPlayScreen.setImageResource(R.mipmap.ic_round_pause);
                startDismissTopBottomTimer();
                startUpdateProgressTimer();
                break;
            case STATE_PAUSED:
                //播放暂停
                mLoading.setVisibility(View.GONE);
                //改变播放按钮图标
                mPlay.setVisibility(View.VISIBLE);
                mPlay.setImageResource(R.mipmap.ic_round_play_circle_outline);
                mDefaultPlay.setImageResource(R.mipmap.ic_round_play_arrow);
                mPlayScreen.setImageResource(R.mipmap.ic_round_play_arrow);
                cancelDismissTopBottomTimer();
                cancelUpdateProgressTimer();
                break;
            case STATE_BUFFERING_PLAYING:
                //缓冲播放
                mLoading.setVisibility(View.VISIBLE);
                //改变播放按钮图标
                startDismissTopBottomTimer();
                break;
            case STATE_BUFFERING_PAUSED:
                //缓冲暂停
                mLoading.setVisibility(View.VISIBLE);
                //改变播放按钮图标
                cancelDismissTopBottomTimer();
                break;
            case STATE_ERROR:
                //播放错误
                cancelUpdateProgressTimer();
                setTopBottomVisible(false);
                mError.setVisibility(View.VISIBLE);
                if (mCurrentMode == MODE_FULL_SCREEN)
                    mTop.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETED:
                //播放完成
                setTopBottomVisible(true);
                cancelUpdateProgressTimer();
                //改变播放按钮图标
                mPlay.setVisibility(View.VISIBLE);
                mPlay.setImageResource(R.mipmap.ic_round_replay);
                break;
        }
    }

    private void onPlayModeChanged(int playMode) {//全屏还是竖屏
        switch (playMode) {
            case MODE_NORMAL:
                mTop.setVisibility(View.GONE);
                mFullCenter.setVisibility(View.GONE);
                mBottom.setVisibility(View.GONE);
                mDefaultMode.setVisibility(View.VISIBLE);
                mDefaultVolume.setVisibility(View.VISIBLE);
                break;
            case MODE_FULL_SCREEN:
                mTop.setVisibility(View.VISIBLE);
                mDefaultMode.setVisibility(View.GONE);
                mDefaultVolume.setVisibility(View.GONE);
                mFullCenter.setVisibility(View.VISIBLE);
                mBottom.setVisibility(View.VISIBLE);
                mCopyAndVolume.setVisibility(View.VISIBLE);
                mLock.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void resetProgressAndTime() {//重置进度和时间显示
        mSeek.setProgress(0);
        mSeek.setSecondaryProgress(0);
        mDefaultSeek.setProgress(0);
        mDefaultSeek.setSecondaryProgress(0);
        String seekTime = com.test.player.JZUtils.stringForTime(0);
        String totalTime = com.test.player.JZUtils.stringForTime(0);
        mPosition.setText(seekTime);
        mTotalTime.setText(totalTime);
        mDefaultPosition.setText(seekTime);
        mDefaultTotalTime.setText(totalTime);
    }

    public void addTextureView() {
        Log.d(TAG, "addTextureView [" + this.hashCode() + "] ");
        if (textureView != null) textureViewContainer.removeView(textureView);
        textureView = new JZTextureView(getContext().getApplicationContext());
        textureView.setSurfaceTextureListener(mediaInterface);

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(textureView, layoutParams);
    }

    public static void setCurrentJzvd(Jzvd jzvd) {
        if (CURRENT_JZVD != null) CURRENT_JZVD.reset();
        CURRENT_JZVD = jzvd;
    }

    /**
     * 多数表现为中断当前播放
     */
    public void reset() {//重置播放器
        Log.i(TAG, "reset " + " [" + this.hashCode() + "] ");
        if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_BUFFERING_PLAYING || mCurrentState == STATE_BUFFERING_PAUSED || mCurrentState == STATE_PAUSED) {
            long position = getCurrentPositionWhenPlaying();
            JZUtils.saveProgress(getContext(), jzDataSource.getCurrentUrl(), position);
        }
        cancelUpdateProgressTimer();
        cancelDismissLockTimer();
        cancelDismissTopBottomTimer();
        onStateNormal();
        textureViewContainer.removeAllViews();//移除

        AudioManager mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.abandonAudioFocus(null);
        JZUtils.scanForActivity(getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mediaInterface != null) {
            mediaInterface.release();
        }
    }

    public void onStateNormal() {
        Log.i(TAG, "onStateNormal " + " [" + this.hashCode() + "] ");
        mCurrentState = STATE_IDLE;
        if (mediaInterface != null) mediaInterface.release();
    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_BUFFERING_PLAYING
                || mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
            try {
                position = mediaInterface.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public long getDuration() {
        long duration = 0;
        if (mediaInterface != null) {
            try {
                duration = mediaInterface.getDuration();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return duration;
            }
        }
        return duration;
    }

    @Override
    public void onClick(View v) {//点击事件
        int i = v.getId();
        if (i == R.id.back || i == R.id.full_bottom_exit) {//返回全屏
            backPress();
        } else if (i == R.id.default_bottom_fullscreen) {//进入全屏
            gotoScreenFullscreen();
        } else if (i == R.id.retry) {//重新播放
            restart();
        } else if (i == R.id.full_lock) {//锁屏
            if (!mIsLock) {
                if (mCurrentState == STATE_COMPLETED ||
                        mCurrentState == STATE_ERROR) {
                    return;
                }
            }
            lockTouchLogic();
        } else if (i == R.id.default_volume || i == R.id.full_volume) {//点击音量
            int volume = getVolume();//当前音量
            int maxVolume = getMaxVolume();//最大音量
            if (volume != 0) {
                setVolume(0);
            } else {
                setVolume(maxVolume);
            }
        } else if (v == mPlay || v == mDefaultPlay || v == mPlayScreen) {//播放或暂停
            if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_BUFFERING_PLAYING) {
                //播放状态，就暂停
                mediaInterface.pause();
                if (mCurrentState == STATE_PLAYING) {
                    mCurrentState = STATE_PAUSED;
                    onPlayStateChanged(mCurrentState);
                    Loger.e("STATE_PAUSED");
                } else if (mCurrentState == STATE_BUFFERING_PLAYING) {
                    mCurrentState = STATE_BUFFERING_PAUSED;
                    onPlayStateChanged(mCurrentState);
                    Loger.e("STATE_BUFFERING_PAUSED");
                }
            } else if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED || mCurrentState == STATE_COMPLETED) {
                restart();
            } else if (mCurrentState == STATE_IDLE) {//刚开始
                startVideo();
            }
        } else if (v == mSelections) {//选集
            List<DownLoadBean> mVideoBeans = DownloadModel.getMyVideo(getContext());
            hideVideoListViewLayout(false);
            showSelectionsView(mVideoBeans);
        } else if (v == mClarity) {//清晰度
            hideVideoListViewLayout(false);
            showClarityView(mCurrentVideo);
        } else if (v == mCopy) {//复制
            if (mSelectionController != null) {
                mSelectionController.onCopy();
            }
        } else if (v == textureViewContainer) {
            if (isShowLayout) {
                hideVideoListViewLayout(false);
            } else if (mIsLock) {
                setLockVisible(!lockVisible);
            } else {
                if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_BUFFERING_PLAYING
                        || mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED || mCurrentState == STATE_COMPLETED) {
                    setTopBottomVisible(!topBottomVisible);
                    if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_BUFFERING_PLAYING) {
                        mPlay.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    private void hideVideoListViewLayout(boolean isVisible) {
        video_condition_layout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        isShowLayout = isVisible;
        if (isVisible) {
            setTopBottomVisible(false);
        }
    }

    private void hideAllGestureLayout() {
        gesture_volume_layout.setVisibility(View.GONE);
        gesture_progress_layout.setVisibility(View.GONE);
        gesture_light_layout.setVisibility(View.GONE);
    }

    private void setVolume(int volume) {//设置当前
        if (mAudioManager != null)
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    private void restart() {
        if (mCurrentState == STATE_PAUSED) {
            mediaInterface.start();
            mCurrentState = STATE_PLAYING;
            onPlayStateChanged(mCurrentState);
            Loger.e("STATE_PLAYING");
        } else if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mediaInterface.start();
            mCurrentState = STATE_BUFFERING_PLAYING;
            onPlayStateChanged(mCurrentState);
            Loger.e("STATE_BUFFERING_PLAYING");
        } else if (mCurrentState == STATE_COMPLETED || mCurrentState == STATE_ERROR) {
//            mediaInterface.reset();
            startVideo();
        } else {
            Loger.e("NiceVideoPlayer在mCurrentState == " + mCurrentState + "时不能调用restart()方法.");
        }
    }

    private void lockTouchLogic() {//锁屏逻辑
        if (!mIsLock) {
            mLock.setImageResource(R.mipmap.ic_round_lock);
            this.mIsLock = true;
            hideAllView();
        } else {
            mLock.setImageResource(R.mipmap.ic_round_lock_open);
            this.mIsLock = false;
            cancelDismissLockTimer();
            setTopBottomVisible(true);
        }
    }

    private void hideAllView() {
        mTop.setVisibility(View.INVISIBLE);
        mPlay.setVisibility(View.INVISIBLE);
        mBottom.setVisibility(View.INVISIBLE);
        setLockVisible(true);
    }

    private void setLockVisible(boolean visible) {
        mFullCenter.setVisibility(View.VISIBLE);
        mCopyAndVolume.setVisibility(View.INVISIBLE);
        mLock.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        lockVisible = visible;
        if (visible) {
            startDismissLockTimer();
        } else {
            cancelDismissLockTimer();
        }
    }

    /**
     * 设置top、bottom的显示和隐藏
     *
     * @param visible true显示，false隐藏.
     */
    private void setTopBottomVisible(boolean visible) {
        if (mCurrentMode == MODE_FULL_SCREEN) {//全屏模式
            if (!mIsLock) {
                mTop.setVisibility(visible ? View.VISIBLE : View.GONE);//显示顶部布局
                mDefaultMode.setVisibility(View.GONE);//隐藏默认底部布局
                mDefaultVolume.setVisibility(View.GONE);//隐藏默认音量
                mPlay.setVisibility(visible ? View.VISIBLE : View.GONE);
                mFullCenter.setVisibility(visible ? View.VISIBLE : View.GONE);
                mCopyAndVolume.setVisibility(visible ? View.VISIBLE : View.GONE);
                mLock.setVisibility(visible ? View.VISIBLE : View.GONE);
                mBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        } else if (mCurrentMode == MODE_NORMAL) {//默认布局
            mTop.setVisibility(View.GONE);
            mDefaultMode.setVisibility(visible ? View.VISIBLE : View.GONE);
            mDefaultVolume.setVisibility(visible ? View.VISIBLE : View.GONE);
            mPlay.setVisibility(visible ? View.VISIBLE : View.GONE);
            mFullCenter.setVisibility(View.GONE);
            mBottom.setVisibility(View.GONE);
            mCopyAndVolume.setVisibility(View.GONE);
            mLock.setVisibility(View.GONE);
        }
        topBottomVisible = visible;
        if (visible) {
            if (mCurrentState != STATE_PAUSED && mCurrentState != STATE_BUFFERING_PAUSED) {
                startDismissTopBottomTimer();
            }
        } else {
            cancelDismissTopBottomTimer();
        }
    }

    private CountDownTimer mDismissTopBottomCountDownTimer;

    /**
     * 开启top、bottom自动消失的timer
     */
    private void startDismissTopBottomTimer() {
        cancelDismissTopBottomTimer();
        if (mDismissTopBottomCountDownTimer == null) {
            mDismissTopBottomCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setTopBottomVisible(false);
                }
            };
        }
        mDismissTopBottomCountDownTimer.start();
    }

    /**
     * 取消top、bottom自动消失的timer
     */
    private void cancelDismissTopBottomTimer() {
        if (mDismissTopBottomCountDownTimer != null) {
            mDismissTopBottomCountDownTimer.cancel();
        }
    }

    private CountDownTimer mDismissLockCountDownTimer;//底部计算器

    /**
     * 开启锁自动消失的timer
     */
    private void startDismissLockTimer() {
        cancelDismissLockTimer();
        if (mDismissLockCountDownTimer == null) {
            mDismissLockCountDownTimer = new CountDownTimer(8000, 8000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    setLockVisible(false);
                }
            };
        }
        mDismissLockCountDownTimer.start();
    }

    /**
     * 取消锁自动消失的timer
     */
    private void cancelDismissLockTimer() {
        if (mDismissLockCountDownTimer != null) {
            mDismissLockCountDownTimer.cancel();
        }
    }

    private Timer mUpdateProgressTimer;
    private TimerTask mUpdateProgressTimerTask;

    /**
     * 开启更新进度的计时器。
     */
    protected void startUpdateProgressTimer() {
        cancelUpdateProgressTimer();
        if (mUpdateProgressTimer == null) {
            mUpdateProgressTimer = new Timer();
        }
        if (mUpdateProgressTimerTask == null) {
            mUpdateProgressTimerTask = new TimerTask() {
                @Override
                public void run() {
                    post(() -> updateProgress());
                }
            };
        }
        mUpdateProgressTimer.schedule(mUpdateProgressTimerTask, 0, 500);
    }

    private void updateProgress() {
        long position = getCurrentPositionWhenPlaying();
        long duration = getDuration();
        mSeek.setMax((int) duration);
        mDefaultSeek.setMax((int) duration);
        mSeek.setProgress((int) position);
        mDefaultSeek.setProgress((int) position);
        String seekTime = com.test.player.JZUtils.stringForTime(position);
        String totalTime = com.test.player.JZUtils.stringForTime(duration);
        mPosition.setText(seekTime);
        mTotalTime.setText(totalTime);
        mDefaultPosition.setText(seekTime);
        mDefaultTotalTime.setText(totalTime);
    }

    /**
     * 取消更新进度的计时器。
     */
    protected void cancelUpdateProgressTimer() {
        if (mUpdateProgressTimer != null) {
            mUpdateProgressTimer.cancel();
            mUpdateProgressTimer = null;
        }
        if (mUpdateProgressTimerTask != null) {
            mUpdateProgressTimerTask.cancel();
            mUpdateProgressTimerTask = null;
        }
    }


    private void gotoScreenFullscreen() {//进入全屏
        ViewGroup vg = (ViewGroup) getParent();//获取包裹本布局的布局(它的父布局)
        vg.removeView(this);//从父布局移除播放器
        cloneAJzvd(vg);
        CONTAINER_LIST.add(vg);
        vg = (ViewGroup) (JZUtils.scanForActivity(getContext())).getWindow().getDecorView();//和他也没有关系
        vg.addView(this, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mCurrentMode = MODE_FULL_SCREEN;
        onPlayModeChanged(mCurrentMode);
        JZUtils.hideStatusBar(getContext());
        JZUtils.setRequestedOrientation(getContext(), FULLSCREEN_ORIENTATION);
        JZUtils.hideSystemUI(getContext());//华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326
    }

    private void cloneAJzvd(ViewGroup vg) {
        try {
            Constructor<Jzvd> constructor = (Constructor<Jzvd>) Jzvd.this.getClass().getConstructor(Context.class);
            Jzvd jzvd = constructor.newInstance(getContext());
            jzvd.setId(getId());
            vg.addView(jzvd);
            jzvd.setUp(jzDataSource.cloneMe(), MODE_NORMAL, mediaInterfaceClass);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void backPress() {//返回全屏
        ViewGroup vg = (ViewGroup) (JZUtils.scanForActivity(getContext())).getWindow().getDecorView();//获取Window
        vg.removeView(this);//从Window窗口移除播放器
        CONTAINER_LIST.getLast().removeAllViews();
        CONTAINER_LIST.getLast().addView(this, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        CONTAINER_LIST.pop();
        mCurrentMode = MODE_NORMAL;
        onPlayModeChanged(mCurrentMode);
        JZUtils.showStatusBar(getContext());
        JZUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
        JZUtils.showSystemUI(getContext());
    }

    long currentMS;
    float DownX, DownY, moveX, moveY, DownMX, DownMY;
    public static final int THRESHOLD = 80;
    boolean mChangeVolume = false;
    boolean mChangePosition = false;
    boolean mChangeBrightness = false;
    protected long mGestureDownPosition;
    protected int mGestureDownVolume;
    protected float mGestureDownBrightness;
    protected long mSeekTimePosition;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!mIsLock) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Loger.e("onTouch surfaceContainer actionDown [" + this.hashCode() + "] ");
                    DownX = event.getX();//float DownX
                    DownY = event.getY();//float DownY
                    moveX = 0;
                    moveY = 0;
                    mChangeVolume = false;
                    mChangePosition = false;
                    mChangeBrightness = false;
                    currentMS = System.currentTimeMillis();//long currentMS     获取系统时间
                    break;
                case MotionEvent.ACTION_MOVE:
                    DownMX = event.getX();
                    DownMY = event.getY();
                    float deltaX = DownMX - DownX;
                    float deltaY = DownMY - DownY;
                    moveX += Math.abs(deltaX);//X轴距离
                    moveY += Math.abs(deltaY);//y轴距离
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);
                    if (!mChangePosition && !mChangeVolume && !mChangeBrightness) {
                        if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                            if (absDeltaX >= THRESHOLD) {
                                cancelUpdateProgressTimer();
                                if (mCurrentState != STATE_ERROR && mCurrentState != STATE_COMPLETED) {
                                    mChangePosition = true;
                                    mGestureDownPosition = getCurrentPositionWhenPlaying();//当前位置
                                    Loger.e("mGestureDownPosition " + mGestureDownPosition);
                                }
                            } else {
                                if (DownX < mScreenWidth * 0.5f) {
                                    mChangeBrightness = true;
                                    WindowManager.LayoutParams lp = com.test.player.JZUtils.getWindow(getContext()).getAttributes();
                                    if (lp.screenBrightness < 0) {
                                        try {
                                            mGestureDownBrightness = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                                            Log.i(TAG, "current system brightness: " + mGestureDownBrightness);
                                        } catch (Settings.SettingNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        mGestureDownBrightness = lp.screenBrightness * 255;
                                        Log.i(TAG, "current activity brightness: " + mGestureDownBrightness);
                                    }
                                } else {
                                    mChangeVolume = true;
                                    mGestureDownVolume = getVolume();//当前音量
                                }
                            }
                        }
                    }
                    if (mChangePosition) {
                        hideAllGestureLayout();
                        long totalTimeDuration = getDuration();
                        mSeekTimePosition = (int) (mGestureDownPosition + deltaX * totalTimeDuration / mScreenWidth);
                        Loger.e("mSeekTimePosition " + mSeekTimePosition);
                        if (mSeekTimePosition > totalTimeDuration)
                            mSeekTimePosition = totalTimeDuration;
                        if (mSeekTimePosition <= 0)
                            mSeekTimePosition = 0;
                        String seekTime = com.test.player.JZUtils.stringForTime(mSeekTimePosition);
                        String totalTime = com.test.player.JZUtils.stringForTime(totalTimeDuration);
                        showProgressViev(deltaX, seekTime, mSeekTimePosition, totalTime, totalTimeDuration);
                    }
                    if (mChangeVolume) {
                        hideAllGestureLayout();
                        deltaY = -deltaY;
                        int max = getMaxVolume();
                        int deltaV = (int) (max * deltaY * 3 / mScreenHeight);
                        setVolume(mGestureDownVolume + deltaV);
                        //dialog中显示百分比
                        int volumePercent = (int) (mGestureDownVolume * 100 / max + deltaY * 3 * 100 / mScreenHeight);
                        showVolumeViev(-deltaY, volumePercent);
                    }
                    if (mChangeBrightness) {
                        hideAllGestureLayout();
                        deltaY = -deltaY;
                        int deltaV = (int) (255 * deltaY * 3 / mScreenHeight);
                        WindowManager.LayoutParams params = com.test.player.JZUtils.getWindow(getContext()).getAttributes();
                        if (((mGestureDownBrightness + deltaV) / 255) >= 1) {//这和声音有区别，必须自己过滤一下负值
                            params.screenBrightness = 1;
                        } else if (((mGestureDownBrightness + deltaV) / 255) <= 0) {
                            params.screenBrightness = 0.01f;
                        } else {
                            params.screenBrightness = (mGestureDownBrightness + deltaV) / 255;
                        }
                        com.test.player.JZUtils.getWindow(getContext()).setAttributes(params);
                        //dialog中显示百分比
                        int brightnessPercent = (int) (mGestureDownBrightness * 100 / 255 + deltaY * 3 * 100 / mScreenHeight);
                        showBrightnessView(brightnessPercent);
                    }
                    Log.i(TAG, "onTouch surfaceContainer actionMove [" + this.hashCode() + "] ");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.i(TAG, "onTouch surfaceContainer actionUp [" + this.hashCode() + "] ");
                    long moveTime = System.currentTimeMillis() - currentMS;//移动时间
                    //判断是否继续传递信号
                    hideAllGestureLayout();
                    if (moveTime > 50 && (moveX > 20 || moveY > 20)) {
                        if (mChangePosition) {
                            long duration = getDuration();
                            int progress = (int) mSeekTimePosition;
                            mDefaultSeek.setMax((int) duration);
                            mSeek.setMax((int) duration);
                            mDefaultSeek.setProgress(progress);
                            mSeek.setProgress(progress);
                            if (mSeekTimePosition == 0) {
                                startVideo();
                            } else {
                                mediaInterface.seekTo(mSeekTimePosition);
                            }
                        }
                        startUpdateProgressTimer();
                        return true; //不再执行后面的事件，在这句前可写要执行的触摸相关代码。点击事件是发生在触摸弹起后
                    }
                    break;
            }
        }
        return false;
    }

    private void showBrightnessView(int brightnessPercent) {
        gesture_light_layout.setVisibility(View.VISIBLE);
        if (brightnessPercent > 100) {
            brightnessPercent = 100;
        } else if (brightnessPercent < 0) {
            brightnessPercent = 0;
        }
        geture_tv_light_percentage.setText(brightnessPercent + "%");
        geture_tv_light_percentageProgress.setProgress(brightnessPercent);
    }

    private void showVolumeViev(float v, int volumePercent) {
        gesture_volume_layout.setVisibility(View.VISIBLE);
        if (volumePercent <= 0) {
            gesture_iv_player_volume.setBackgroundResource(R.mipmap.ic_round_volume_off);
        } else {
            gesture_iv_player_volume.setBackgroundResource(R.mipmap.ic_round_volume_up);
        }
        if (volumePercent > 100) {
            volumePercent = 100;
        } else if (volumePercent < 0) {
            volumePercent = 0;
        }
        geture_tv_volume_percentage.setText(volumePercent + "%");
        geture_tv_volume_percentage_progress.setProgress(volumePercent);
    }

    private void showProgressViev(float deltaX, String seekTime, long mSeekTimePosition, String totalTime, long totalTimeDuration) {
        gesture_progress_layout.setVisibility(View.VISIBLE);
        mPosition.setText(seekTime);
        mTotalTime.setText(totalTime);
        mDefaultPosition.setText(seekTime);
        mDefaultTotalTime.setText(totalTime);
        mSeek.setMax((int) totalTimeDuration);
        mDefaultSeek.setMax((int) totalTimeDuration);
        mSeek.setProgress(totalTimeDuration <= 0 ? 0 : (int) mSeekTimePosition);
        mDefaultSeek.setProgress(totalTimeDuration <= 0 ? 0 : (int) mSeekTimePosition);
        if (deltaX > 0) {
            gesture_iv_progress.setImageResource(R.drawable.jz_forward_icon);
        } else {
            gesture_iv_progress.setImageResource(R.drawable.jz_backward_icon);
        }
        geture_tv_progress_time.setText(seekTime + "/" + totalTime);
    }


    public int getMaxVolume() {//最大音量
        if (mAudioManager != null) {
            return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    public int getVolume() {//当前音量
        if (mAudioManager != null) {
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        return 0;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mediaInterface.seekTo(seekBar.getProgress());
        startDismissTopBottomTimer();
    }

    public void setBufferProgress(int percent) {//缓冲进度
        if (percent >= 0 && percent <= 100) {
            int mBufferPercentage = (int) (getDuration() * percent / 100);
            mSeek.setSecondaryProgress(mBufferPercentage);
            mDefaultSeek.setSecondaryProgress(mBufferPercentage);
        }
    }

    public void onInfo(int what, int extra) {//播放信息
        if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START
                || what == IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START) {
            // 播放器开始渲染
            mCurrentState = STATE_PLAYING;
            onPlayStateChanged(mCurrentState);
//                Loger.e("onInfo ——> MEDIA_INFO_VIDEO_RENDERING_START：STATE_PLAYING");
        } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
            // MediaPlayer暂时不播放，以缓冲更多的数据
            if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                mCurrentState = STATE_BUFFERING_PAUSED;
                Loger.e("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PAUSED");
            } else {
                mCurrentState = STATE_BUFFERING_PLAYING;
                Loger.e("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PLAYING");
            }
            onPlayStateChanged(mCurrentState);
        } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            // 填充缓冲区后，MediaPlayer恢复播放/暂停
            if (mCurrentState == STATE_BUFFERING_PLAYING) {
                mCurrentState = STATE_PLAYING;
                onPlayStateChanged(mCurrentState);
                Loger.e("onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PLAYING");
            }
            if (mCurrentState == STATE_BUFFERING_PAUSED) {
                mCurrentState = STATE_PAUSED;
                onPlayStateChanged(mCurrentState);
                Loger.e("onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PAUSED");
            }
        } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            // 视频旋转了extra度，需要恢复
            if (textureView != null) {
                textureView.setRotation(extra);
//                    Loger.e("视频旋转角度：" + extra);
            }
        }
    }

    public void onSeekComplete() {//精度完成
    }

    public void onError(int what, int extra) {//播放错误
        if (what != -38 && what != -2147483648 && extra != -38 && extra != -2147483648) {
            mCurrentState = STATE_ERROR;
            onPlayStateChanged(mCurrentState);
            Loger.e("onError ——> STATE_ERROR ———— what：" + what + ", extra: " + extra);
        }
    }

    public void onVideoSizeChanged(int videoWidth, int videoHeight) {
        Log.i(TAG, "onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        if (textureView != null) {
//            if (videoRotation != 0) {
//                textureView.setRotation(videoRotation);
//            }
            textureView.setVideoSize(videoWidth, videoHeight);
        }
    }

    public void onAutoCompletion() {//播放完成
        Runtime.getRuntime().gc();
        mCurrentState = STATE_COMPLETED;
        onPlayStateChanged(mCurrentState);
    }

    private boolean continueFromLastPosition = false;
    private long skipToPosition;

    public void onPrepared() {//
        mCurrentState = STATE_PREPARED;
        onPlayStateChanged(mCurrentState);
        Loger.e("onPrepared ——> STATE_PREPARED");
        mediaInterface.start();
        // 从上次的保存位置播放
        if (continueFromLastPosition) {
            long savedPlayPosition = JZUtils.getSavedProgress(getContext(), jzDataSource.getCurrentUrl());
            mediaInterface.seekTo(savedPlayPosition);
        }
        // 跳到指定位置播放
        if (skipToPosition != 0) {
            mediaInterface.seekTo(skipToPosition);
        }
    }


    //选择清晰度播放
    private void showClarityView(DownLoadBean videoBean) {
        if (videoBean == null) {
            emptyView.setVisibility(View.VISIBLE);
            hideVideoListViewLayout(true);
        } else {
            emptyView.setVisibility(View.GONE);
            final List<MovieHLS> movieHLSListView = new Gson().fromJson(videoBean.movieHLS, new TypeToken<List<MovieHLS>>() {
            }.getType());
            for (int j = 0; j < movieHLSListView.size(); j++) {
                MovieHLS movieHLS1 = movieHLSListView.get(j);
                if (!TextUtils.isEmpty(movieHLS1.resolution) && videoBean.downLoad_hls == Integer.parseInt(movieHLS1.resolution)) {
                    movieHLSListView.get(j).resolutionUrl = videoBean.path;
                    if (mCurrentClarity == -1)
                        mCurrentClarity = Integer.parseInt(movieHLS1.resolution);
                    movieHLSListView.get(j).isSelect = true;
                    movieHLSListView.get(j).isLocalPath = true;
                } else {
                    movieHLSListView.get(j).isSelect = false;
                    movieHLSListView.get(j).isLocalPath = false;
                }
            }
            if (movieHLSListView.size() > 0) {
                isShowLayout = true;
                mClarityAdapter = new ClarityAdapter(getContext());
                mClarityAdapter.update(movieHLSListView, mCurrentClarity);
                condition_lv.setAdapter(mClarityAdapter);
                hideVideoListViewLayout(true);
                condition_lv.setOnItemClickListener((parent, view, position, id) -> {
                    MovieHLS movieHLS = movieHLSListView.get(position);
                    if (mCurrentClarity != Integer.parseInt(movieHLS.resolution)) {//防止选重
                        checkClarityPosition(position, movieHLSListView);
                    }
                    hideVideoListViewLayout(false);
                });
            }
        }
    }

    //设置选中的位置，将其他位置设置为未选
    public void checkClarityPosition(int position, List<MovieHLS> movieHLSListView) {
        for (int i = 0; i < movieHLSListView.size(); i++) {
            MovieHLS movieHLS = movieHLSListView.get(position);
            if (position == i) {// 设置已选位置
                movieHLS.isSelect = true;
                mCurrentClarity = Integer.parseInt(movieHLS.resolution);
            } else {
                movieHLS.isSelect = false;
            }
        }
        if (mSelectionController != null) {
            mSelectionController.onSelectClarity(mCurrentVideo, movieHLSListView.get(position).resolutionUrl, getCurrentPositionWhenPlaying());
        }
        mClarityAdapter.updateSelectClarity(mCurrentClarity);
        setTextViewClarity(mCurrentClarity);
    }

    //选集
    private void showSelectionsView(final List<DownLoadBean> mVideoBeans) {
        if (mVideoBeans != null) {
            isShowLayout = true;
            mSelectionsAdapter = new SelectionsAdapter(getContext());
            mSelectionsAdapter.update(mVideoBeans, mCurrentVideo);
            condition_lv.setAdapter(mSelectionsAdapter);
            if (mVideoBeans.size() <= 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
            hideVideoListViewLayout(true);
            condition_lv.setOnItemClickListener((parent, view, position, id) -> {
                if (mCurrentVideo == null) return;
                DownLoadBean bean = mVideoBeans.get(position);
                if (mCurrentVideo.video_id != bean.video_id)//防止选重
                    checkSelectionsPosition(position, bean);
                hideVideoListViewLayout(false);
            });
        }
    }

    //设置选中的位置，将其他位置设置为未选
    public void checkSelectionsPosition(int position, DownLoadBean bean) {
        mSelectionsAdapter.updateSelectBean(bean);
        mCurrentVideo = bean;
        mCurrentClarity = bean.downLoad_hls;
        if (mSelectionController != null) {
//            unRequestOrientationEventListener();
            mSelectionController.onSelectVideo(bean);
        }
        setTextViewClarity(mCurrentClarity);
    }

    @SuppressLint("SetTextI18n")
    private void setTextViewClarity(int clarity) {
        mClarity.setText(clarity + "p");
    }

    private ISelectionController mSelectionController;

    //选集
    public void setOnSelectionController(ISelectionController playController) {
        mSelectionController = playController;
    }

    public interface ISelectionController {

        void onVideoPrepared();

        void onSelectVideo(DownLoadBean bean);//选择播放的视频

        void onSelectClarity(DownLoadBean bean, String videoPath, long playPosition);//选择播放的视频

        void onCopy();//复制
    }

    public void pausedPlayer() {
        if (mediaInterface != null && (mCurrentState == STATE_PLAYING || mCurrentState == STATE_BUFFERING_PLAYING)) {
            if (mCurrentState == STATE_PLAYING) {
                mediaInterface.pause();
                mCurrentState = STATE_PAUSED;
                onPlayStateChanged(mCurrentState);
            }
            if (mCurrentState == STATE_BUFFERING_PLAYING) {
                mediaInterface.pause();
                mCurrentState = STATE_BUFFERING_PAUSED;
                onPlayStateChanged(mCurrentState);
                Loger.e("STATE_BUFFERING_PAUSED");
            }
        }
    }

    public void resumedPlayer() {
        if (mediaInterface != null && (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED)) {
            restart();
        }
    }
}
