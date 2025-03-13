package com.saferide;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // FCM 메시지가 notification을 포함하면 알림을 자동 처리
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body);  // 알림을 수동으로 처리
        }
    }

    private void sendNotification(String title, String body) {
        // Intent to launch MainActivity when the notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // PendingIntent 플래그를 API 31 이상에 맞게 설정
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        String channelId = "default_channel"; // Firebase에서 설정한 채널 ID와 동일하게 설정
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // NotificationCompat.Builder 사용하여 알림을 구성
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)  // drawable 폴더에 저장된 아이콘 사용
                        .setContentTitle(title)  // FCM에서 받은 title 사용
                        .setContentText(body)    // FCM에서 받은 body 사용
                        .setAutoCancel(true)     // 클릭 시 자동으로 알림이 제거됨
                        .setSound(defaultSoundUri) // 기본 알림 소리
                        .setContentIntent(pendingIntent)  // 알림 클릭 시 실행될 인텐트
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위 높음 (상단 알림)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 알림 내용을 공개적으로 표시
                        .setFullScreenIntent(pendingIntent, true)  // Full-screen 팝업 알림
                        .setCategory(NotificationCompat.CATEGORY_ALARM); // 알람 카테고리 설정 (알람으로 인식)

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android Oreo 이상에서는 알림 채널이 필요합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Default Channel",  // 채널 이름
                    NotificationManager.IMPORTANCE_HIGH);  // 알림의 중요도 설정
            channel.setDescription("Default channel for notifications");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);  // 잠금화면에서도 공개적으로 표시
            notificationManager.createNotificationChannel(channel);
        }

        // 알림을 화면에 표시
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
