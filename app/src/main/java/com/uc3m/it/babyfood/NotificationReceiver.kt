package com.uc3m.it.babyfood

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationReceiver : BroadcastReceiver() {

    //GEMINI
    //este metodo se ejecuta cuando salta la notificacion
    override fun onReceive(context: Context, intent: Intent) {
        val foodName = intent.getStringExtra("food_name") ?: return //string que llega de AlarmUtils
        val babyName = intent.getStringExtra("baby_name") ?: "Tu bebé"

        val channelId = "new_food_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Introducción de alimentos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos sobre nuevos alimentos del bebé"
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //comprueba si el usuario dio permiso
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) return
        }
        //crea la notificacion
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("¡Nuevo alimento!")
            .setContentText("$babyName ya puede probar: $foodName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        //muestras la notificacion
        NotificationManagerCompat.from(context)
            .notify(foodName.hashCode(), notification)
    }
}