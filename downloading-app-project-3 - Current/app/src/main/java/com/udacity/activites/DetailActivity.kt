package com.udacity.activites

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import com.udacity.R
import com.udacity.databinding.ActivityDetailBinding
import com.udacity.utilities.cancelNotifications
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.view.*
import java.util.*
import kotlin.concurrent.schedule

class DetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailBinding
    lateinit var motionLayout: MotionLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(toolbar)

        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotifications()

        val title = intent.getStringExtra("EXTRA_TITLE")
        binding.content.titleTextView.text = title

        val status = intent.getIntExtra("EXTRA_STATUS", -1)
        when(status) {
            DownloadManager.STATUS_SUCCESSFUL -> binding.content.downloadTextView.apply { setTextColor(Color.GREEN)
                text = getString(R.string.successStatus)
            }
            DownloadManager.STATUS_FAILED -> binding.content.downloadTextView.apply { setTextColor(Color.RED)
                text = getString(R.string.failedStatus)
            }
        }

        motionLayout = binding.content.motionLayout

        motionLayout.transitionToEnd{
            motionLayout.transitionToStart()
        }

        binding.content.backButton.setOnClickListener() {

            motionLayout.transitionToEnd {
                val contentIntent = Intent(applicationContext, MainActivity::class.java)
                contentIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                this.finish()
                startActivity(contentIntent)
            }

        }

    }

}

