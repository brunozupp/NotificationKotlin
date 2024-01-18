package com.novelitech.notificationkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.novelitech.notificationkotlin.databinding.ActivityMainBinding

/**
 * Pending Intent -> It permits that another app execute a piece of code from my app.
 * It can be used to open my app when clicking in the notification
 */

class MainActivity : AppCompatActivity() {

    // These need to be unique
    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    val NOTIFICATION_ID = 0

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(this).run {
            // this will add the activity on the notification's click. And if I had more activities in the stack, when I click in the back button, it will respect the flow and go back to previous activities
            addNextIntentWithParentStack(intent)
            // This FLAG means that when this pending intent already exist then I want to update its data with new data

            getPendingIntent(
                0,
                if(Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Awesome notification")
            .setContentText("This is the content text")
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Don't confound with IMPORTANCE from NotificationChannel
            .setContentIntent(pendingIntent) // To open MainActivity when click the notification
            .build()

        val notificationManager = NotificationManagerCompat.from(this)

        binding.btnNotification.setOnClickListener {

            requestPermissionPostNotifications()

            if(hasPostNotificationsPermission()) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun requestPermissionPostNotifications() {

        if(!hasPostNotificationsPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    private fun hasPostNotificationsPermission() : Boolean {
        val currentStatusPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)

        return currentStatusPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationChannel() {
        // Before Android Orion It wasn't needed to create a Channel. So I need to verify
        // this condition about the version

        // It's running in Android Orion or Later
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT // The importance is about if it will have sound, or if it will appear in the statusBar
            ).apply {
                lightColor = Color.GREEN
                enableLights(true)
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}