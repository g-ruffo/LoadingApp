package com.udacity.activites

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.udacity.R
import com.udacity.databinding.ActivityMainBinding
import com.udacity.objects.ButtonState
import com.udacity.utilities.sendNotification
import com.udacity.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action
    lateinit var viewModel: MainViewModel
    private var downloadStatus = ""

    private var downloadUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.content.viewModel = viewModel

        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        viewModel.selectedDownloadRadioButton.observe(this, Observer { url ->
            downloadUrl = url
            Log.v("MainActivityURL", downloadUrl)

        })

        custom_button.setOnClickListener {
            download()
        }

        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_title)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val notificationManager = ContextCompat.getSystemService(
                application, NotificationManager::class.java
            ) as NotificationManager

            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (id == downloadID) {
                downloadStatus = "Success"
                notificationManager.sendNotification(
                    "Download Is Completed!!", viewModel.selectedTitleRadioButton.value,
                    downloadStatus,
                    application
                )

                Toast.makeText(applicationContext, "Download Is Completed!!", Toast.LENGTH_SHORT)
                    .show()


            } else if (id != downloadID) {
                downloadStatus = "Failed"
                notificationManager.sendNotification(
                    "Download Failed", viewModel.selectedTitleRadioButton.value,
                    downloadStatus,
                    application
                )
                Toast.makeText(applicationContext, "Download Failed", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun download() {

        if (downloadUrl.isNullOrEmpty()) {
            Toast.makeText(applicationContext, "You Need To Make a Selection", Toast.LENGTH_SHORT)
                .show()
            custom_button.isSelectionValid(false)
            return

        } else if (!URLUtil.isValidUrl(downloadUrl)) {
            Toast.makeText(applicationContext, "Not A Valid URL", Toast.LENGTH_SHORT).show()
            custom_button.isSelectionValid(false)

            return
        }
        custom_button.isSelectionValid(true)


        val request =
            DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setRequiresCharging(false)
                .setAllowedOverMetered(false)
                .setAllowedOverRoaming(false)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        Log.v("MainActivityDownloadId", downloadID.toString())

    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download Complete!"

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

}
