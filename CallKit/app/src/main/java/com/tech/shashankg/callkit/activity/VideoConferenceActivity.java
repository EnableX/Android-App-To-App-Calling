package com.tech.shashankg.callkit.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.tech.shashankg.callkit.R;
import com.tech.shashankg.callkit.model.HorizontalViewModel;
import com.tech.shashankg.callkit.utilities.OnDragTouchListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import enx_rtc_android.Controller.EnxActiveTalkerViewObserver;
import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxRoom;
import enx_rtc_android.Controller.EnxRoomObserver;
import enx_rtc_android.Controller.EnxRtc;
import enx_rtc_android.Controller.EnxStream;
import enx_rtc_android.Controller.EnxStreamObserver;

public class VideoConferenceActivity extends AppCompatActivity implements EnxRoomObserver, EnxStreamObserver, View.OnClickListener, EnxActiveTalkerViewObserver {
    EnxRtc enxRtc;
    String token;
    String name;
    EnxPlayerView enxPlayerView;
    FrameLayout moderator;
    FrameLayout participant;
    ImageView disconnect;
    ImageView mute, video, camera, volume;
    private TextView audioOnlyText, dummyText;
    EnxRoom enxRooms;
    boolean isVideoMuted = false;
    boolean isAudioMuted = false;
    RelativeLayout rl;
    //    ArrayList<UserModel> userArrayList;
    Gson gson;
    EnxStream localStream;
    int PERMISSION_ALL = 1;

    List<HorizontalViewModel> list;
    private RecyclerView mHorizontalRecyclerView;
    private LinearLayoutManager horizontalLayoutManager;
    private int screenWidth;
    ActionBar actionBar;
    RelativeLayout bottomView;
    boolean touchView;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO
    };

    RecyclerView mRecyclerView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);

        actionBar = getSupportActionBar();

        getPreviousIntent();
        actionBar.setTitle(name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            } else {
                initialize();
            }
        }
    }

    private void initialize() {
        setUI();
        setClickListener();
//        userArrayList = new ArrayList<>();
        list = new ArrayList<>();
        gson = new Gson();
        enxRtc = new EnxRtc(this, this, this);
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObjet(), getRoomInfo(), new JSONArray());
        enxPlayerView = new EnxPlayerView(this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
        Log.e("localStream", localStream.toString());
        localStream.attachRenderer(enxPlayerView);
        moderator.addView(enxPlayerView);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setClickListener() {
        disconnect.setOnClickListener(this);
        mute.setOnClickListener(this);
        video.setOnClickListener(this);
        camera.setOnClickListener(this);
        volume.setOnClickListener(this);
        moderator.setOnTouchListener(new OnDragTouchListener(moderator));
    }

    private void setUI() {
        moderator = (FrameLayout) findViewById(R.id.moderator);
        participant = (FrameLayout) findViewById(R.id.participant);
        disconnect = (ImageView) findViewById(R.id.disconnect);
        mute = (ImageView) findViewById(R.id.mute);
        video = (ImageView) findViewById(R.id.video);
        camera = (ImageView) findViewById(R.id.camera);
        volume = (ImageView) findViewById(R.id.volume);
        dummyText = (TextView) findViewById(R.id.dummyText);
        audioOnlyText = (TextView) findViewById(R.id.audioonlyText);
        rl = (RelativeLayout) findViewById(R.id.rl);
        bottomView = (RelativeLayout) findViewById(R.id.bottomView);

        audioOnlyText.setVisibility(View.GONE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels / 3;
    }

    private JSONObject getLocalStreamJsonObjet() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("audio", true);
            jsonObject.put("video", true);
            jsonObject.put("data", true);
            JSONObject videoSize = new JSONObject();
            videoSize.put("minWidth", 720);
            videoSize.put("minHeight", 480);
            videoSize.put("maxWidth", 1280);
            videoSize.put("maxHeight", 720);
            jsonObject.put("videoSize", videoSize);
            jsonObject.put("audioMuted", "false");
            jsonObject.put("videoMuted", "false");
            JSONObject attributes = new JSONObject();
            attributes.put("name", "myStream");
            jsonObject.put("attributes", attributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject getRoomInfo() {
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("allow_reconnect",true);
            jsonObject.put("number_of_attempts",3);
            jsonObject.put("timeout_interval",15);
            jsonObject.put("activeviews","view");//view

            JSONObject object = new JSONObject();
            object.put("audiomute",true);
            object.put("videomute",true);
            object.put("bandwidth",true);
            object.put("screenshot",true);
            object.put("avatar",true);

            object.put("iconColor", getResources().getColor(R.color.colorPrimary));
            object.put("iconHeight",30);
            object.put("iconWidth",30);
            object.put("avatarHeight",200);
            object.put("avatarWidth",200);
            jsonObject.put("playerConfiguration",object);

            jsonObject.put("forceTurn",false);
            jsonObject.put("chat_only",false);
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getPreviousIntent() {
        if (getIntent() != null) {
            token = getIntent().getStringExtra("token");
            name = getIntent().getStringExtra("name");
        }
    }

    @Override
    public void onRoomConnected(EnxRoom enxRoom, JSONObject jsonObject) {
        enxRooms = enxRoom;
        if (enxRoom != null) {
            enxRooms.publish(localStream);
            enxRoom.setActiveTalkerViewObserver(this);
        }
    }

    @Override
    public void onRoomError(final JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onUserConnected(JSONObject jsonObject) {
        Log.e("userConnected", jsonObject.toString());
    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject) {
        Log.e("userConnected", jsonObject.toString());
        enxRooms.disconnect();
    }

    @Override
    public void onPublishedStream(EnxStream enxStream) {
    }

    @Override
    public void onUnPublishedStream(EnxStream enxStream) {

    }

    @Override
    public void onStreamAdded(EnxStream enxStream) {
        if (enxStream != null) {
            enxRooms.subscribe(enxStream);
        }
    }

    @Override
    public void onSubscribedStream(EnxStream enxStream) {
    }

    @Override
    public void onUnSubscribedStream(EnxStream enxStream) {

    }

    public void onRoomDisConnected(JSONObject jsonObject) {
        this.finish();
    }

    @Override
    public void onActiveTalkerList(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        if (recyclerView == null) {
            participant.removeAllViews();

        } else {
            participant.removeAllViews();
            participant.addView(recyclerView);

        }
    }

    EnxPlayerView activePlayerView;



    @Override
    public void onEventError(final JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEventInfo(JSONObject jsonObject) {

    }

    @Override
    public void onNotifyDeviceUpdate(String s) {

    }

    @Override
    public void onAcknowledgedSendData(JSONObject jsonObject) {

    }


    @Override
    public void onMessageReceived(JSONObject jsonObject) {

    }

    @Override
    public void onUserDataReceived(JSONObject jsonObject) {

    }

    @Override
    public void onSwitchedUserRole(JSONObject jsonObject) {

    }

    @Override
    public void onUserRoleChanged(JSONObject jsonObject) {

    }

    @Override
    public void onConferencessExtended(JSONObject jsonObject) {

    }

    @Override
    public void onConferenceRemainingDuration(JSONObject jsonObject) {

    }

    @Override
    public void onAckDropUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckDestroy(JSONObject jsonObject) {

    }

    @Override
    public void onAckPinUsers(JSONObject jsonObject) {

    }

    @Override
    public void onAckUnpinUsers(JSONObject jsonObject) {

    }

    @Override
    public void onPinnedUsers(JSONObject jsonObject) {

    }

    @Override
    public void onRoomAwaited(EnxRoom enxRoom, JSONObject jsonObject) {

    }

    @Override
    public void onUserAwaited(JSONObject jsonObject) {

    }

    @Override
    public void onAckForApproveAwaitedUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckForDenyAwaitedUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckAddSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onAckRemoveSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onUpdateSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onRoomBandwidthAlert(JSONObject jsonObject) {

    }

    @Override
    public void onAudioEvent(JSONObject jsonObject) {
        try {
            String message = jsonObject.getString("msg");
            if (!isAudioMuted) {
                if (message.equalsIgnoreCase("Audio Off")) {
                    mute.setImageResource(R.drawable.mute);
                    isAudioMuted = true;
                }
            } else {
                if (message.equalsIgnoreCase("Audio On")) {
                    mute.setImageResource(R.drawable.unmute);
                    isAudioMuted = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoEvent(final JSONObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    String message = jsonObject.getString("msg");
                    if (message.equalsIgnoreCase("Video on")) {
                        video.setImageResource(R.drawable.video_visible);
                        isVideoMuted = false;
                    } else if (message.equalsIgnoreCase("Video off")) {
                        video.setImageResource(R.drawable.video_off);
                        isVideoMuted = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onReceivedData(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamAudioMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamAudioUnMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamVideoMute(JSONObject jsonObject) {

    }

    @Override
    public void onRemoteStreamVideoUnMute(JSONObject jsonObject) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.disconnect:
                if (enxRooms != null) {
                    if (enxPlayerView != null) {
                        enxPlayerView.release();
                        enxPlayerView = null;
                    }
                    enxRooms.disconnect();
                } else {
                    finish();
                }
                break;
            case R.id.mute:
                if (localStream != null) {
                    if (!isAudioMuted) {
                        localStream.muteSelfAudio(true);
                    } else {
                        localStream.muteSelfAudio(false);
                    }
                }
                break;
            case R.id.video:
                if (localStream != null) {
                    if (!isVideoMuted) {
                        isVideoMuted = true;
                        localStream.muteSelfVideo(true);
                    } else {
                        isVideoMuted = false;
                        localStream.muteSelfVideo(false);
                    }
                }
                break;
            case R.id.camera:
                localStream.switchCamera();
                camera.setImageResource(R.drawable.camera);
                break;
            case R.id.volume:
                if (enxRooms != null) {
                    showRadioButtonDialog();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                } else {
                    Toast.makeText(this, "Please enable permissions to further proceed.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showRadioButtonDialog() {

        // custom dialog
        final Dialog dialog = new Dialog(VideoConferenceActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radiogroup);
        List<String> stringList = new ArrayList<>();  // here is list

        List<String> deviceList = enxRooms.getDevices();
        for (int i = 0; i < deviceList.size(); i++) {
            stringList.add(deviceList.get(i));
        }
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        String selectedDevice = enxRooms.getSelectedDevice();
        if (selectedDevice != null) {
            for (int i = 0; i < stringList.size(); i++) {
                RadioButton rb = new RadioButton(VideoConferenceActivity.this); // dynamically creating RadioButton and adding to RadioGroup.
                rb.setText(stringList.get(i));
                rg.addView(rb);
                if (selectedDevice.equalsIgnoreCase(stringList.get(i))) {
                    rb.setChecked(true);
                }

            }
            dialog.show();
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        Log.e("selected RadioButton->", btn.getText().toString());
                        enxRooms.switchMediaDevice(btn.getText().toString());
                       // enxRooms.setAudioDevice(btn.getText().toString());
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (enxRooms != null) {
            enxRooms = null;
        }
        if (enxRtc != null) {
            enxRtc = null;
        }
    }


}
