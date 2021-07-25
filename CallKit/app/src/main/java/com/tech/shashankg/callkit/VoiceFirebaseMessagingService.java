package com.tech.shashankg.callkit;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tech.shashankg.callkit.activity.DashboardActivity;

import java.util.Map;

public class VoiceFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "VoiceFCMService";
    private static final String NOTIFICATION_ID_KEY = "NOTIFICATION_ID";
    private static final String CALL_SID_KEY = "CALL_SID";
    private static final String VOICE_CHANNEL = "default";

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Received onMessageReceived()");
        Log.d(TAG, "Bundle data: " + remoteMessage.getData());
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String phoneNumber = null;
        String message = null;
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            phoneNumber = data.get("localphoneNumber");
            message = data.get("message");
        }
        final int notificationId = (int) System.currentTimeMillis();
        if (message.equalsIgnoreCase("answer")) {
            VoiceFirebaseMessagingService.this.sendCallInviteToActivity(message, phoneNumber, notificationId);
        } else if (message.equalsIgnoreCase("reject")) {
            VoiceFirebaseMessagingService.this.sendCallInviteToActivity(message, phoneNumber, notificationId);
        } else if (message.equalsIgnoreCase("not_available")) {
            VoiceFirebaseMessagingService.this.sendCallInviteToActivity(message, phoneNumber, notificationId);
        } else {
            VoiceFirebaseMessagingService.this.notify(notificationId, phoneNumber);
            VoiceFirebaseMessagingService.this.sendCallInviteToActivity(message, phoneNumber, notificationId);
        }
    }

    private void notify(int notificationId, String phoneNumber) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setAction(DashboardActivity.ACTION_INCOMING_CALL);
        intent.putExtra(DashboardActivity.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        /*
         * Pass the notification id and call sid to use as an identifier to cancel the
         * notification later
         */
        Bundle extras = new Bundle();
        extras.putInt(NOTIFICATION_ID_KEY, notificationId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel callInviteChannel = new NotificationChannel(VOICE_CHANNEL,
                    "Primary Voice Channel", NotificationManager.IMPORTANCE_DEFAULT);
            callInviteChannel.setLightColor(Color.GREEN);
            callInviteChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(callInviteChannel);

            Notification notification =
                    buildNotification(phoneNumber + " is calling.",
                            pendingIntent,
                            extras);
            notificationManager.notify(notificationId, notification);
        } else {
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(phoneNumber + " is calling.")
                            .setAutoCancel(true)
                            .setExtras(extras)
                            .setContentIntent(pendingIntent)
                            .setGroup("test_app_notification")
                            .setColor(Color.rgb(214, 10, 37));

            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }

    /*
     * Send the CallInvite to the VoiceActivity. Start the activity if it is not running already.
     */
    private void sendCallInviteToActivity(String message, String number, int notificationId) {
        Intent intent = new Intent(this, DashboardActivity.class);
        if (message.equalsIgnoreCase("answer")) {
            intent.setAction(DashboardActivity.INCOMING_CALL_ACCEPT);
        } else if (message.equalsIgnoreCase("reject")) {
            intent.setAction(DashboardActivity.ACTION_CANCEL_CALL);
        } else if (message.equalsIgnoreCase("not_available")) {
            intent.setAction(DashboardActivity.INCOMING_CALL_NOT_AVAILABLE);
        } else {
            intent.setAction(DashboardActivity.ACTION_INCOMING_CALL);
        }
        intent.putExtra("roomId", message);
        intent.putExtra("callingPhoneNumber", number);
        intent.putExtra(DashboardActivity.INCOMING_CALL_NOTIFICATION_ID, notificationId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    /**
     * Build a notification.
     *
     * @param text          the text of the notification
     * @param pendingIntent the body, pending intent for the notification
     * @param extras        extras passed with the notification
     * @return the builder
     */
    @TargetApi(Build.VERSION_CODES.O)
    public Notification buildNotification(String text, PendingIntent pendingIntent, Bundle extras) {
        return new Notification.Builder(getApplicationContext(), VOICE_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setExtras(extras)
                .setAutoCancel(true)
                .build();
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}
