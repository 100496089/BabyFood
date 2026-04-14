package com.uc3m.it.babyfood

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*

//GEMINI
object AlarmUtils {
    //se ejecuta cuando guardas la fehca del bebe
    fun scheduleAllNotifications(context: Context) {
        val birthDate = getBirthDate(context) ?: return

        val foods = listOf(
            //6 meses
            Triple("Manzana", 6, 0),
            Triple("Plátano", 6, 7), // a los 6 meses + 7 dias: zanahoria
            Triple("Patata", 6, 14),
            Triple("Arroz", 6, 21),
            //7 meses
            Triple("Pera", 7, 0),
            Triple("Pimiento", 7, 3),
            Triple("Pollo", 7, 7),
            Triple("Lentejas", 7, 14),
            Triple("Merluza", 7, 21),
            //8 meses
            Triple("Yema Cocida", 8, 0),
            Triple("Trigo", 8, 7),
            Triple("Quinoa", 8, 14),
            Triple("Garbanzos", 8, 21),
            //9 meses
            Triple("Cerdo", 9, 0),
            Triple("Sardina", 9, 7),
            Triple("Brócoli", 9, 14),
            Triple("Pasta", 9, 21),
            //10 meses
            Triple("Conejo", 10, 0),
            Triple("Yogur natural", 10, 7),
            Triple("Frutos secos (triturados)", 10, 14),
            Triple("Queso fresco", 10, 21),
            //11 meses
            Triple("Galletas sin azúzar", 11, 0),
            Triple("Ternera", 11, 7),
            Triple("Gambas", 11, 14),
            Triple("Tomate", 11, 21),
            //12 meses
            Triple("Leche entera", 12, 0),
            Triple("Quesos tiernos", 12, 10),
            Triple("Pulpo", 12, 20)

        )
        //recorre cada alimento
        foods.forEachIndexed { index, food ->
            val foodName = food.first
            val month = food.second
            val offsetDays = food.third
            //convierte fehca, mes, dias en milisegundos
            val triggerMillis = calculateTriggerMillis(birthDate, month, offsetDays)

            if (triggerMillis > System.currentTimeMillis()) { //para que no sean fechas pasadas
                scheduleNotification( //creas la alarma
                    context = context,
                    triggerAtMillis = triggerMillis,
                    foodName = foodName,
                    requestCode = 1000 + index
                )
            }
        }
    }


    private fun getBirthDate(context: Context): Date? {
        val prefs = context.getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        val birthDateStr = prefs.getString("fecha", null) ?: return null

        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) //convierte string en fecha
            sdf.parse(birthDateStr)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateTriggerMillis(
        birthDate: Date,
        months: Int,
        days: Int
    ): Long {
        // calcular la fecha de la alarma, en nuestro caso se enviara a las 9:00
        val calendar = Calendar.getInstance().apply {
            time = birthDate
            add(Calendar.MONTH, months)
            add(Calendar.DAY_OF_MONTH, days)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis // devuelve la fecha en milisegundos
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(
        context: Context,
        triggerAtMillis: Long,
        foodName: String,
        requestCode: Int
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("food_name", foodName)// pasamos el nombre del alimento a NotificationReceiver
        }

        val pendingIntent = PendingIntent.getBroadcast( //Es como un “recordatorio guardado” que Android ejecutará luego
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager //Sistema de alarmas de Android

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}