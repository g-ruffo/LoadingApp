package com.udacity.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.udacity.R
import com.udacity.activites.DetailActivity
import com.udacity.activites.MainActivity

// Notification ID.
private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

fun NotificationManager.sendNotification(messageBody: String, title: String?, status: Int, applicationContext: Context) {

    val contentIntent = Intent(applicationContext, DetailActivity::class.java).also {
        it.putExtra("EXTRA_TITLE", title)
        it.putExtra("EXTRA_STATUS", status)
    }
    val contentPendingIntent = PendingIntent.getActivity(applicationContext, NOTIFICATION_ID, contentIntent,
    PendingIntent.FLAG_UPDATE_CURRENT)

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notification_channel_id)
    ).setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setAutoCancel(true)
        .addAction(R.drawable.download_button,
            applicationContext.getString(R.string.checkStatus),
            contentPendingIntent
        )


    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}

