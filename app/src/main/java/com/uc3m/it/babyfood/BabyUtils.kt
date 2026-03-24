package com.uc3m.it.babyfood

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object BabyUtils {
    // Variable global para almacenar la edad del bebé: BabyUtils.babyAge
    var babyAge: String = "Edad del bebé"

    fun updateAge(context: Context) {
        val prefs = context.getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        val birthDateStr = prefs.getString("fecha", null)
        
        if (birthDateStr.isNullOrEmpty()) {
            babyAge = "Fecha no configurada"
            return
        }

        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = sdf.parse(birthDateStr) ?: return
            
            val today = Calendar.getInstance()
            val birth = Calendar.getInstance().apply { time = birthDate }

            var years = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            var months = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
            var days = today.get(Calendar.DAY_OF_MONTH) - birth.get(Calendar.DAY_OF_MONTH)

            // Ajuste si el mes actual es anterior al mes de nacimiento o mismo mes pero día anterior
            if (months < 0 || (months == 0 && days < 0)) {
                years--
                months += 12
            }
            if (days < 0) {
                months--
                // No necesitamos el cálculo exacto de días para la edad en meses normalmente
            }

            babyAge = when {
                years >= 1 -> {
                    if (months > 0) "$years años y $months meses" else "$years años"
                }
                months >= 1 -> "$months meses"
                else -> "Recién nacido"
            }
        } catch (e: Exception) {
            babyAge = "Error en formato"
        }
    }
}
