package com.test.bms_video_player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.test.jiaoplayer.JzvdStd;
import com.test.player.NiceVideoPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;
import other.DownLoadBean;
import other.MovieHLS;

public class VideoView implements PlatformView, MethodChannel.MethodCallHandler, JzvdStd.ISelectionController {
    //    private final NiceVideoPlayer jzvdStd;
//    private VideoPlayerController mController;
    private final MethodChannel methodChannel;
    private final PluginRegistry.Registrar registrar;
    private EventChannel eventChannel;
    private EventChannel.EventSink eventSink;
    private DownLoadBean currentBean;
    private int comeFrom = 0;
    private String currentResolution;

    private View bmsVideo;
    private JzvdStd jzvdStd;

    VideoView(Context context, int viewId, Object args, PluginRegistry.Registrar registrar) {
        this.registrar = registrar;
//        this.jzvdStd = getJzvStd(registrar, args);
        bmsVideo = LayoutInflater.from(registrar.activity()).inflate(R.layout.layout_videoview, null);
        jzvdStd = bmsVideo.findViewById(R.id.videoplayer);
//        mController = new VideoPlayerController(registrar.activity());
        jzvdStd.setOnSelectionController(this);
        this.methodChannel = new MethodChannel(registrar.messenger(), "bms_video_player_" + viewId);
        this.methodChannel.setMethodCallHandler(this);
        //本地发送信息
        eventChannel = new EventChannel(registrar.messenger(), "flutter_bms_video_player_event_" + viewId);
        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object o, EventChannel.EventSink sink) {
                eventSink = sink;
            }

            @Override
            public void onCancel(Object o) {
                eventSink = null;
            }
        });
    }

    public View getView() {
        return bmsVideo;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method) {
            case "loadUrl":
                Map arg = methodCall.arguments();
                String info = arg.get("movieInfo").toString();
                String playUrl = arg.get("playUrl").toString();
                currentResolution = arg.get("currentResolution").toString();
                String resolutions = arg.get("resolutions").toString();
                comeFrom = Integer.parseInt(arg.get("comeFrom").toString());
                currentBean = new Gson().fromJson(info, DownLoadBean.class);
                currentBean.original_id = Integer.parseInt(currentBean.id);
//                currentBean.downLoad_hls = Integer.parseInt(currentResolution);
                currentBean.picture = currentBean.icon;
                currentBean.movieHLS = resolutions;
                play(playUrl, currentBean);
                break;
            case "androidBack":
                if (jzvdStd != null && !jzvdStd.getIsLock()) {
                    jzvdStd.backPress();
                }
                break;
            case "pausedPlayer"://暂停播放
                if (jzvdStd != null) {
                    jzvdStd.pausedPlayer();
                }
                break;
            case "resumedPlayer"://恢复播放
                if (jzvdStd != null) {
                    jzvdStd.resumedPlayer();
                }
                break;
            default:
                result.notImplemented();
        }

    }

    private void play(String playUrl, DownLoadBean bean) {
//        mController.setTitle(bean.name);
//        mController.setDownLoadBean(bean);
//        mController.setResolution(String.valueOf(currentResolution));
//        jzvdStd.setUp(playUrl, null);
//        jzvdStd.setController(mController);
//        jzvdStd.start();
        jzvdStd.setResolution(String.valueOf(currentResolution));
        jzvdStd.setTitle(bean.name);
        jzvdStd.setDownLoadBean(bean);
        jzvdStd.setUp(playUrl.replace("https://", "http://")
                , bean.name);
        jzvdStd.startVideo();
    }

    @Override
    public void onInputConnectionLocked() {

    }

    @Override
    public void onInputConnectionUnlocked() {

    }

    @Override
    public void dispose() {
        if (jzvdStd != null) {
            jzvdStd.unRequestOrientationEventListener();
            jzvdStd.reset();
        }

    }

    private NiceVideoPlayer getJzvStd(PluginRegistry.Registrar registrar, Object args) {
        NiceVideoPlayer view = (NiceVideoPlayer) LayoutInflater.from(registrar.activity()).inflate(R.layout.jz_video, null);
        return view;
    }

    @Override
    public void onVideoPrepared() {
        if (eventSink != null) {
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("action", "prepared");
            args.put("comeFrom", comeFrom);
            args.put("movie_id", currentBean.original_id);
            eventSink.success(args);
        }
    }

    @Override
    public void onSelectVideo(DownLoadBean bean) {
        comeFrom = 1;
        currentBean = bean;
        if (eventSink != null) {
            List<Map<String, Object>> listAll = new ArrayList();
            if (null != bean.movieHLS && !TextUtils.isEmpty(bean.movieHLS)) {
                List<MovieHLS> movieHLS = new Gson().fromJson(bean.movieHLS, new TypeToken<List<MovieHLS>>() {
                }.getType());
                for (int j = 0; j < movieHLS.size(); j++) {
                    MovieHLS movieHLS1 = movieHLS.get(j);
                    if (!TextUtils.isEmpty(movieHLS1.resolution) && bean.downLoad_hls == Integer.parseInt(movieHLS1.resolution)) {
                        movieHLS.get(j).resolutionUrl = bean.path;
                        movieHLS.get(j).isSelect = true;
                        movieHLS.get(j).isLocalPath = true;
                    } else {
                        movieHLS.get(j).isSelect = false;
                        movieHLS.get(j).isLocalPath = false;
                    }
                }
                bean.movieHLS = new Gson().toJson(movieHLS);
            }
            currentBean = bean;
            Map<String, Object> args = getDownloadVideoMap(bean);
            args.put("action", "SelectVideo");
            eventSink.success(args);
        }
//        jzvdStd.releasePlayer();
//        mController.setTitle(bean.name);
//        jzvdStd.setUp(bean.path, null);
//        jzvdStd.setController(mController);
//        jzvdStd.start();

        jzvdStd.setTitle(bean.name);
        jzvdStd.setUp(bean.path
                , bean.name);
        jzvdStd.startVideo();
    }

    @Override
    public void onSelectClarity(DownLoadBean bean, String videoPath, long playPosition) {
        if (videoPath == null) return;
//        jzvdStd.releasePlayer();
//        mController.setTitle(bean.name);
//        jzvdStd.setUp(videoPath, null);
//        jzvdStd.setController(mController);
//        jzvdStd.start(playPosition);
//        jzvdStd.changeUrl(videoPath.replace("https://", "http://")
//                , bean.name, playPosition);
        jzvdStd.setTitle(bean.name);
        jzvdStd.setUp(videoPath.replace("https://", "http://")
                , bean.name);
        jzvdStd.startVideo(playPosition);
        if (eventSink != null) {
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("action", "SelectClarity");
            args.put("clarity", jzvdStd.getCurrentClarity());
            eventSink.success(args);
        }
    }

    @SuppressLint("ShowToast")
    @Override
    public void onCopy() {
        if (eventSink != null) {
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("action", "AndroidCopy");
            eventSink.success(args);
        }
    }

    private Map<String, Object> getDownloadVideoMap(DownLoadBean aRequest) {
        Map<String, Object> args = new HashMap<>();
        args.put("video_id", aRequest.video_id);
        args.put("video_tag", aRequest.video_tag);
        args.put("insert_time", aRequest.insert_time);
        args.put("id", aRequest.original_id);
        args.put("video_access_address", aRequest.web_url);
        args.put("video_title", aRequest.name);
        args.put("video_duration", aRequest.videoTime);
        args.put("video_cover", aRequest.icon);
        args.put("video_urlLow", aRequest.video_urlLow);
        args.put("video_urlhigh", aRequest.video_urlhigh);
        args.put("video_hls", aRequest.video_hls);
        args.put("video_hls_url", aRequest.video_hls_url);
        args.put("cate_id", aRequest.cate_id);
        args.put("views", aRequest.views);
        args.put("downLoad_hls", aRequest.downLoad_hls);
        args.put("localViews", aRequest.localViews);
        args.put("localId", aRequest.id);
        args.put("title", aRequest.getName());
        args.put("downloadtaskid", aRequest.downloadId + "");
        args.put("state", aRequest.downloadState + "");
        args.put("MIMEType", aRequest.fileFormat);
        args.put("savepath", aRequest.path);
        args.put("speed", aRequest.downSpeed + "");
        args.put("totalBytesWritten", aRequest.currentSize + "");
        args.put("totalBytesExpected", aRequest.totalSize + "");
        args.put("movieHLS", aRequest.movieHLS + "");
        return args;
    }
}