package com.udacity.activites

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.udacity.databinding.ActivityDetailBinding
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*
import kotlinx.android.synthetic.main.content_detail.view.*
import kotlinx.coroutines.delay
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

class DetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailBinding

    lateinit var motionLayout: MotionLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        binding = com.udacity.databinding.ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(toolbar)

        val title = intent.getStringExtra("EXTRA_TITLE")
        binding.content.titleTextView.text = title

        val status = intent.getStringExtra("EXTRA_STATUS")
        binding.content.downloadTextView.text = status

        motionLayout = binding.content.motionLayout

        motionLayout.transitionToEnd()

//        startAnimation()

        Timer().schedule(100) {

            motionLayout.transitionToStart()
        }



        binding.content.backButton.setOnClickListener() {

            motionLayout.transitionToEnd()

            Timer().schedule(100) {
                val contentIntent = Intent(applicationContext, MainActivity::class.java)


                startActivity(contentIntent)

            }

        }

    }


    }

