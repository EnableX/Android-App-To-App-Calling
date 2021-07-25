package com.tech.shashankg.callkit.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.tech.shashankg.callkit.R;
import com.tech.shashankg.callkit.SoundPoolManager;
import com.tech.shashankg.callkit.web_communication.WebCall;
import com.tech.shashankg.callkit.web_communication.WebConstants;
import com.tech.shashankg.callkit.web_communication.WebResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, WebResponse {

    private String token;
    private String fcm_token;
    private String room_Id;

    private FloatingActionButton callActionFab;
    private RelativeLayout registerDeviceView;
    private Button submitButton;
    private EditText phoneNumber;
    private SoundPoolManager soundPoolManager;

    private static final String TAG = "MainActivity";
    public static final String ACTION_INCOMING_CALL = "ACTION_INCOMING_CALL";
    public static final String INCOMING_CALL_NOTIFICATION_ID = "INCOMING_CALL_NOTIFICATION_ID";
    public static final String ACTION_CANCEL_CALL = "ACTION_CANCEL_CALL";
    public static final String INCOMING_CALL_ACCEPT = "INCOMING_CALL_ACCEPT";
    public static final String INCOMING_CALL_NOT_AVAILABLE = "INCOMING_CALL_NOT_AVAILABLE";
    public static final String ACTION_FCM_TOKEN = "ACTION_FCM_TOKEN";

    private int activeCallNotificationId;
//    private String callingRoomId;

    private NotificationManager notificationManager;
    private AlertDialog alertDialog;
    private boolean isReceiverRegistered = false;
    private VoiceBroadcastReceiver voiceBroadcastReceiver;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isAppFirst;
    AlertDialog callingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        setView();
        setClickListener();
        getSupportActionBar().setTitle("Quick App");
        sharedPreferences = getSharedPreferences("callKit", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isAppFirst = sharedPreferences.getBoolean("isAppFirst", false);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!isAppFirst) {
            callActionFab.hide();
            registerDeviceView.setVisibility(View.VISIBLE);
        } else {
            callActionFab.show();
            registerDeviceView.setVisibility(View.GONE);
        }


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        soundPoolManager = SoundPoolManager.getInstance(this);

        voiceBroadcastReceiver = new VoiceBroadcastReceiver();
        registerReceiver();

        handleIncomingCallIntent(getIntent());

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
//To do//
                            return;
                        }

// Get the Instance ID token//
                        fcm_token = task.getResult().getToken();
//                        String msg = getString("fcm_token"", token);
//                        Log.d(TAG, token);

                    }
                });
    }

    private void setView() {
        callActionFab = findViewById(R.id.call_action_fab);
        registerDeviceView = findViewById(R.id.deviceRegistration);
        submitButton = findViewById(R.id.submit);
        phoneNumber = findViewById(R.id.phone_number);
    }

    private void setClickListener() {
        callActionFab.setOnClickListener(this::onClick);
        submitButton.setOnClickListener(this::onClick);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingCallIntent(intent);
        Log.d(TAG, "Received onNewIntent");


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call_action_fab:
                alertDialog = createCallDialog(callClickListener(), cancelCallClickListener(null), DashboardActivity.this);
                alertDialog.show();
                break;
            case R.id.submit:
                registerDevice(fcm_token, phoneNumber.getText().toString());
            default:
                break;
        }
    }

    @Override
    public void onWebResponse(String response, int callCode) {
        switch (callCode) {
            case WebConstants.getRoomIdCode:
                onGetRoomIdSuccess(response);
                break;
            case WebConstants.getTokenURLCode:
                onGetTokenSuccess(response);
                break;
            case WebConstants.fcmToken_Register_URLCode:
                handleFCMTokenRegistrattion(response.toString());
                break;
            case WebConstants.fcm_SendingRoomId_URLCode:
                if (contact.getText().toString().length() == 10) {
                    callingDialog(contact.getText().toString());
                }
                break;
            case WebConstants.fcm_Answer_URLCode:
                Toast.makeText(DashboardActivity.this, "Call is connecting", Toast.LENGTH_SHORT).show();
                break;
            case WebConstants.fcm_Reject_URLCode:
                finish();
                break;
        }
    }

    @Override
    public void onWebResponseError(String error, int callCode) {
        Log.e("errorDashboard", error);
    }

    private void onGetRoomIdSuccess(String response) {
        Log.e("responseDashboard", response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            room_Id = jsonObject.optJSONObject("room").optString("room_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRoomTokenWebCall(String roomId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "Participant1");
            jsonObject.put("role", "participant");
            jsonObject.put("user_ref", "2236");
            jsonObject.put("roomId", roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new WebCall(this, this, jsonObject, WebConstants.getTokenURL, WebConstants.getTokenURLCode, false, false, false).execute();

    }

    private void onGetTokenSuccess(String response) {
        Log.e("responseToken", response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.optString("result").equalsIgnoreCase("0")) {
                token = jsonObject.optString("token");
                Intent intent = new Intent(DashboardActivity.this, VideoConferenceActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("name", room_Id);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, jsonObject.optString("error"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleFCMTokenRegistrattion(String message) {
        try {
            callActionFab.show();
            registerDeviceView.setVisibility(View.GONE);
            editor.putBoolean("isAppFirst", true);
            editor.putString("phone_number", phoneNumber.getText().toString());
            editor.commit();
            Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRoomIdToFirebase(String localNumber) {
        try {
            room_Id = "5df2442cd5a618d6d2bd774c";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", room_Id);
            jsonObject.put("phone_number", contact.getText().toString());
            jsonObject.put("localPhonenumber", localNumber);
            new WebCall(this, this, jsonObject, "http://192.168.137.1:3001/posts/sendMessage", WebConstants.fcm_SendingRoomId_URLCode, false, false, true).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendAnsweredCallNotification(String callingNumber) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", "answer");
            jsonObject.put("phone_number", callingNumber);
            jsonObject.put("type", "answer");
            jsonObject.put("localPhonenumber", "answer");
            new WebCall(this, this, jsonObject, "http://192.168.137.1:3001/posts/sendMessage", WebConstants.fcm_Answer_URLCode, false, false, true).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendRejectCallNotification(String callingPhoneNumber) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", "reject");
            jsonObject.put("phone_number", callingPhoneNumber);
            jsonObject.put("localPhonenumber", "reject");
            jsonObject.put("type", "reject");
            new WebCall(this, this, jsonObject, "http://192.168.137.1:3001/posts/sendMessage", WebConstants.fcm_Reject_URLCode, false, false, true).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotAvailableCallNotification(String callingPhoneNumber) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", "did not pick a call");
            jsonObject.put("phone_number", callingPhoneNumber);
            jsonObject.put("type", "not_available");
            jsonObject.put("localPhonenumber", "not_available");
            new WebCall(this, this, jsonObject, "http://192.168.137.1:3001/posts/sendMessage", WebConstants.fcm_Reject_URLCode, false, false, true).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingCallIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_INCOMING_CALL)) {
                soundPoolManager.playIcomingRinging();
                String callingRoomId = intent.getStringExtra("roomId");
                String callingPhoneNumber = intent.getStringExtra("callingPhoneNumber");
                alertDialog = createIncomingCallDialog(DashboardActivity.this,
                        answerCallClickListener(callingRoomId, callingPhoneNumber),
                        cancelCallClickListener(callingPhoneNumber), callingPhoneNumber);
                alertDialog.setCancelable(false);
                alertDialog.show();
                activeCallNotificationId = intent.getIntExtra(INCOMING_CALL_NOTIFICATION_ID, 0);
                new CountDownTimer(30000, 1000) {
                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                        if (alertDialog.isShowing()) {
                            alertDialog.dismiss();
                            soundPoolManager.stopIncomingRinging();
                            notificationManager.cancel(activeCallNotificationId);
                            sendNotAvailableCallNotification(callingPhoneNumber);
                        }
                    }
                }.start();
            } else if (intent.getAction().equals(ACTION_CANCEL_CALL)) {
                if (callingDialog != null && callingDialog.isShowing()) {
                    soundPoolManager.stopOutgoingRinging();
                    callingDialog.cancel();
                    Toast.makeText(this, "Call is rejected", Toast.LENGTH_SHORT).show();
                }
            } else if (intent.getAction().equals(INCOMING_CALL_ACCEPT)) {
                if (callingDialog != null && callingDialog.isShowing()) {
                    soundPoolManager.stopOutgoingRinging();
                    callingDialog.cancel();
                    getRoomTokenWebCall(room_Id);
                }
            } else if (intent.getAction().equals(INCOMING_CALL_NOT_AVAILABLE)) {
                if (callingDialog != null && callingDialog.isShowing()) {
                    soundPoolManager.stopOutgoingRinging();
                    callingDialog.cancel();
                    Toast.makeText(this, "Call is not attended by caller", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static AlertDialog createIncomingCallDialog(
            Context context,
            DialogInterface.OnClickListener answerCallClickListener,
            DialogInterface.OnClickListener cancelClickListener, String number) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setIcon(R.drawable.ic_launcher_background);
        alertDialogBuilder.setTitle("Incoming Call");
        alertDialogBuilder.setPositiveButton("Accept", answerCallClickListener);
        alertDialogBuilder.setNegativeButton("Reject", cancelClickListener);
        alertDialogBuilder.setMessage(number + " is calling.");
        return alertDialogBuilder.create();
    }

    private DialogInterface.OnClickListener answerCallClickListener(String roomId, String callingNumber) {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                soundPoolManager.stopIncomingRinging();
                alertDialog.dismiss();
                answer(roomId, callingNumber);
            }
        };
    }

    private DialogInterface.OnClickListener cancelCallClickListener(String callingPhoneNumber) {
        return new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (callingPhoneNumber != null) {
                    soundPoolManager.stopIncomingRinging();
                    notificationManager.cancel(activeCallNotificationId);
                    sendRejectCallNotification(callingPhoneNumber);
                }
                alertDialog.dismiss();
            }
        };
    }

    private DialogInterface.OnClickListener callClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String registeredPhoneNumber = sharedPreferences.getString("phone_number", null);

                if (contact.getText() != null && contact.getText().length() == 10) {
                    if (registeredPhoneNumber != null && registeredPhoneNumber.equalsIgnoreCase(contact.getText().toString())) {
                        Toast.makeText(DashboardActivity.this, "Can not call on the same number", Toast.LENGTH_SHORT).show();
                    } else {
                        soundPoolManager.playOutgoingRinging();
                        sendRoomIdToFirebase(registeredPhoneNumber);
//                    new WebCall(DashboardActivity.this, DashboardActivity.this, null, WebConstants.getRoomId, WebConstants.getRoomIdCode, false, true,false).execute();
                    }
                } else {
                    Toast.makeText(DashboardActivity.this, "Phone Number is not valid", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void answer(String roomId, String callingNumber) {
        notificationManager.cancel(activeCallNotificationId);
        sendAnsweredCallNotification(callingNumber);
        getRoomTokenWebCall(roomId);
    }

    static EditText contact;

    public static AlertDialog createCallDialog(final DialogInterface.OnClickListener callClickListener,
                                               final DialogInterface.OnClickListener cancelClickListener,
                                               final Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setIcon(R.drawable.ic_launcher_background);
        alertDialogBuilder.setTitle("Call");
        alertDialogBuilder.setPositiveButton("Call", callClickListener);
        alertDialogBuilder.setNegativeButton("Cancel", cancelClickListener);

        alertDialogBuilder.setCancelable(false);

        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.dialog_call, null);
        contact = (EditText) dialogView.findViewById(R.id.contact);
        contact.setHint("phone number");
        alertDialogBuilder.setView(dialogView);

        return alertDialogBuilder.create();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    public void onDestroy() {
        soundPoolManager.release();
        super.onDestroy();
    }


    private void registerReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_INCOMING_CALL);
            intentFilter.addAction(ACTION_CANCEL_CALL);
            intentFilter.addAction(ACTION_FCM_TOKEN);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    voiceBroadcastReceiver, intentFilter);
            isReceiverRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceBroadcastReceiver);
            isReceiverRegistered = false;
        }
    }

    private class VoiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ACTION_FCM_TOKEN)) {
                callActionFab.hide();
                registerDeviceView.setVisibility(View.VISIBLE);
                fcm_token = intent.getStringExtra("token");
            }
        }
    }

    private void registerDevice(String token, String number) {
        if (fcm_token != null && fcm_token.length() > 0 && phoneNumber.getText() != null && phoneNumber.getText().length() == 10) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("token", token);
                jsonObject.put("phone_number", number);
                new WebCall(this, this, jsonObject, "http://192.168.137.1:3001/posts/registerDevice", WebConstants.fcmToken_Register_URLCode, false, false, true).execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Number is not valid", Toast.LENGTH_SHORT).show();
        }
    }

    private void callingDialog(String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Calling to " + message);

        callingDialog = alertDialog.create();
        callingDialog.show();
    }
}
