package com.udacity.activites

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.udacity.BuildConfig
import com.udacity.R
import com.udacity.databinding.ActivityMainBinding
import com.udacity.objects.ButtonState
import com.udacity.utilities.Constants
import com.udacity.utilities.sendNotification
import com.udacity.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    private var downloadUrl: String = ""
    private var downloadID: Long = 0
    var fileUriString = ""
    var fileTitle = ""


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
            // If no selection fade button background
            if (url.isNullOrEmpty()){
                custom_button.alpha = .3f
            } else{
                custom_button.alpha = 1f
            }
            downloadUrl = url

        })

        binding.content.button.setOnClickListener{

            if (fileUriString.substring(0, 7).matches("file://".toRegex())) {
                fileUriString =  fileUriString.substring(7)
            }
//            fileUriString.replace("%20", " ")
            Log.v("FileUriTitle", fileUriString)

            val path = File(Environment.getExternalStorageDirectory(), this?.getString(R.string.app_name))
            val file = File(path, fileTitle)

            Log.v("FilePath", path.toString())
            Log.v("Filefile", file.toString())

            val uri = FileProvider.getUriForFile(
                Objects.requireNonNull(applicationContext), BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            Log.v("FileUri", uri.toString())

            val mimes = contentResolver.getType(uri)

            val i = Intent(Intent.ACTION_VIEW)
            i.setDataAndType(
                uri,
                mimes
            )
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(i)
        }

        custom_button.setOnClickListener {
            download()
        }

        createChannel(
            Constants.CHANNEL_ID,
            getString(R.string.notification_title)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadManager: DownloadManager =
                getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            val id = intent!!.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val query = DownloadManager.Query().setFilterById(id)
            val cursor = downloadManager.query(query)


            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                fileUriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                fileTitle = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                sendDownloadNotification(status)
            }
            cursor.close()
            custom_button.changeButtonState(ButtonState.Completed)

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
                .setDescription(getString(R.string.app_description))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setRequiresCharging(false)
                .setAllowedOverMetered(false)
                .setAllowedOverRoaming(false)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${viewModel.selectedTitleRadioButton.value}" + ".zip")

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

        Log.v("MainActivityDownloadId", downloadID.toString())

    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.download_success_notification_message)

            val notificationManager =
                getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun sendDownloadNotification(status: Int) {
        val notificationManager = ContextCompat.getSystemService(
            application, NotificationManager::class.java
        ) as NotificationManager

        var title = viewModel.selectedTitleRadioButton.value

        if (title.isNullOrEmpty()){
            title = viewModel.selectedDownloadRadioButton.value
        }

        when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> {
                notificationManager.sendNotification(
                    getString(R.string.download_success_notification_message), title,
                    DownloadManager.STATUS_SUCCESSFUL,
                    application
                )
                Toast.makeText(applicationContext, getString(R.string.downloadCompleteToast), Toast.LENGTH_SHORT)
                    .show()
            }

            DownloadManager.STATUS_FAILED -> {
                notificationManager.sendNotification(
                    "Download Failed", title,
                    DownloadManager.STATUS_FAILED,
                    application
                )
                Toast.makeText(applicationContext, getString(R.string.downloadFailedToast), Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

//    suspend fun checkUrl(){
//        withContext(Dispatchers.IO) {
//            var url = URL(downloadUrl)
//
//            val c: URLConnection = url.openConnection()
//            val contentType: String = c.getContentType()
//            Log.v("checkValidUrl", contentType)
//
//        }
//    }



}
