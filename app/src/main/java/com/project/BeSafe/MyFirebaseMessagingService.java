package com.project.BeSafe;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String district = remoteMessage.getData().get("district");
        String incident = remoteMessage.getData().get("incident");

        // Retrieve the user's current district from shared preferences or a database
        String currentDistrict = getCurrentDistrictFromPreferences();

        if (district != null && district.equals(currentDistrict)) {
            sendNotification(incident);
            saveNotificationToDatabase(incident);
        }
    }

    private void sendNotification(String incidentDetails) {
        Intent intent = new Intent(this, Dashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setContentTitle("New Incident in Your District")
                .setContentText(incidentDetails)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void saveNotificationToDatabase(String incidentDetails) {
        // Implement the logic to save the notification to the local database
    }

    private String getCurrentDistrictFromPreferences() {
        SharedPreferences preferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        return preferences.getString("currentDistrict", null);
    }
}

