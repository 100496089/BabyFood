package com.uc3m.it.babyfood

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        val foods = context.resources.getStringArray(R.array.notification_foods)

        val calendar = java.util.Calendar.getInstance()
        val weekOfYear = calendar.get(java.util.Calendar.WEEK_OF_YEAR) //cambiar por la edad del bebe

        // Elegimos el alimento usando el resto de la división (para no salirnos del array)
        val foodToday = foods[weekOfYear % foods.size]
        val channelId = "semanal_channel"
        val mBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("¡Hora de revisar!")
            .setContentText("Añadir $foodToday a la alimentación del bebé")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        //sistema que muestra la notificacion
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Canal de HelloAlarmAppMov",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(1, mBuilder.build())
    }
}