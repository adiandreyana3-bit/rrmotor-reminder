package com.rrmotor.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "RR_MOTOR_REMINDER";

    @Override
    public void onReceive(Context context, Intent intent) {

        String documentId =
                intent.getStringExtra("documentId");

        String nomorWA =
                intent.getStringExtra("nomorWA");

        String nama =
                intent.getStringExtra("nama");

        String motor =
                intent.getStringExtra("motor");

        long kmMaksimal =
                intent.getLongExtra(
                        "kmMaksimal",
                        0
                );

        long kmTerakhir =
                intent.getLongExtra(
                        "kmTerakhir",
                        0
                );

        long bulan =
                intent.getLongExtra(
                        "bulan",
                        1
                );

        // Intent untuk membuka WhatsApp langsung
        Intent waIntent =
                new Intent(
                        context,
                        WhatsAppRedirectActivity.class
                );

        waIntent.putExtra(
                "documentId",
                documentId
        );

        waIntent.putExtra(
                "nomorWA",
                nomorWA
        );

        waIntent.putExtra(
                "nama",
                nama
        );

        waIntent.putExtra(
                "motor",
                motor
        );

        waIntent.putExtra(
                "kmMaksimal",
                kmMaksimal
        );

        waIntent.putExtra(
                "kmTerakhir",
                kmTerakhir
        );

        waIntent.putExtra(
                "bulan",
                bulan
        );

        waIntent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        documentId == null
                                ? 0
                                : documentId.hashCode(),
                        waIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        // Untuk Android 8 ke atas
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "RR MOTOR Reminder",
                            NotificationManager
                                    .IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Notifikasi pengingat ganti oli RR MOTOR"
            );

            manager.createNotificationChannel(
                    channel
            );
        }

        String judul =
                "🏍️ RR MOTOR - Reminder ganti oli";

        String isi =
                "Tekan untuk membuka WhatsApp pelanggan";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(judul)
                        .setContentText(isi)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(
                                                "Reminder ganti oli sudah jatuh tempo.\n"
                                                        + "Tekan notifikasi untuk membuka WhatsApp."
                                        )
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true)
                        .setContentIntent(
                                pendingIntent
                        );

        int notificationId =
                documentId == null
                        ? (int) System.currentTimeMillis()
                        : documentId.hashCode();

        manager.notify(
                notificationId,
                builder.build()
        );
    }
}
