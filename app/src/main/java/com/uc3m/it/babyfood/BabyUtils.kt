package com.uc3m.it.babyfood

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object BabyUtils {
    // Variable global para almacenar la edad del bebé: BabyUtils.babyAge
    var babyAge: String = "Edad del bebé"
    val foodNames = listOf(
        "Zanahoria", "Brócoli", "Patata", "Tomate", "Calabaza", "Calabacín",
        "Berenjena", "Guisantes", "Judías verdes", "Puerro", "Batata",
        "Coliflor", "Pepino",

        "Plátano", "Manzana", "Pera", "Naranja", "Uva", "Aguacate", "Mango",
        "Papaya", "Fresa", "Arándanos", "Ciruela", "Melocotón", "Sandía",
        "Melón", "Limon", "Kiwi",

        "Pollo", "Vacuno", "Cerdo", "Pavo", "Conejo", "Huevo",
        "Merluza", "Salmón", "Lentejas", "Garbanzos", "Alubias", "Tofu",

        "Maíz", "Arroz", "Avena", "Pasta", "Quinoa", "Cuscús", "Pan",
        "Leche", "Yogur natural", "Queso fresco",

        "Aceite de oliva"
    )
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

    //añadido para filtrar según la edad del bebé: ChatGPT
    fun getAgeInMonths(): Int {
        return when {
            //asociemos la edad calculada a meses
            babyAge.contains("Recién") -> 0
            babyAge.contains("meses") -> {
                //divide en partes el texto y se queda con el priemr elemento
                babyAge.split(" ")[0].toIntOrNull() ?: 0
            }
            babyAge.contains("años") -> {
                val years = babyAge.split(" ")[0].toIntOrNull() ?: 0
                years * 12
            }
            else -> 0
        }
    }

    //Funciones para el cálculo de percentiles (ayuda de Gemini)
    fun getMonthsBetween(birthDateStr: String, targetDateStr: String): Int {
        try {
            val sdfBirth = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfTarget = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val birth = Calendar.getInstance().apply { time = sdfBirth.parse(birthDateStr)!! }
            val target = Calendar.getInstance().apply { time = sdfTarget.parse(targetDateStr)!! }

            val years = target.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            val months = target.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
            return (years * 12) + months
        } catch (e: Exception) { return 0 }
    }

    // Percentiles de peso (OMS) - Rangos aproximados P15 - P85
    // Retorna Pair(PesoMinimo, PesoMaximo)
    fun getWeightRange(months: Int, isBoy: Boolean): Pair<Float, Float> {
        val age = if (months > 24) 24 else months // Limitamos a 2 años
        return if (isBoy) {
            when (age) {
                0 -> Pair(2.9f, 3.9f)
                1 -> Pair(3.9f, 5.1f)
                2 -> Pair(4.9f, 6.3f)
                3 -> Pair(5.7f, 7.2f)
                4 -> Pair(6.2f, 7.8f)
                5 -> Pair(6.7f, 8.4f)
                6 -> Pair(7.1f, 8.8f)
                7 -> Pair(7.4f, 9.2f)
                8 -> Pair(7.7f, 9.6f)
                9 -> Pair(8.0f, 9.9f)
                10 -> Pair(8.2f, 10.2f)
                11 -> Pair(8.4f, 10.5f)
                12 -> Pair(8.6f, 10.8f)
                else -> Pair(8.6f + (age-12)*0.2f, 10.8f + (age-12)*0.3f)
            }
        } else {
            when (age) {
                0 -> Pair(2.8f, 3.7f)
                1 -> Pair(3.6f, 4.8f)
                2 -> Pair(4.5f, 5.8f)
                3 -> Pair(5.2f, 6.6f)
                4 -> Pair(5.7f, 7.3f)
                5 -> Pair(6.1f, 7.8f)
                6 -> Pair(6.4f, 8.2f)
                7 -> Pair(6.7f, 8.6f)
                8 -> Pair(7.0f, 9.0f)
                9 -> Pair(7.2f, 9.3f)
                10 -> Pair(7.5f, 9.6f)
                11 -> Pair(7.7f, 9.9f)
                12 -> Pair(7.9f, 10.1f)
                else -> Pair(7.9f + (age-12)*0.2f, 10.1f + (age-12)*0.3f)
            }
        }
    }

    fun getMinAgeForFood(food: String): Int {
        return when (food) {

            // 6 meses -> papillas suaves de frutas y verduras
            "Zanahoria", "Brócoli", "Patata", "Tomate", "Calabaza", "Calabacín",
            "Berenjena", "Guisantes", "Judías verdes", "Puerro", "Batata",
            "Coliflor", "Pepino",
            "Plátano", "Manzana", "Pera", "Naranja", "Aguacate", "Mango",
            "Papaya", "Ciruela", "Melocotón", "Sandía", "Melón",
            "Arroz", "Leche", "Aceite de oliva" -> 5

            // 7-8 meses -> carnes, huevo, papillas más espesas
            "Pollo", "Vacuno", "Cerdo", "Pavo", "Conejo", "Huevo",
            "Avena" -> 6

            // 9-10 meses -> alimentos blandos en trozos
            "Merluza", "Salmón", "Lentejas", "Garbanzos", "Alubias", "Tofu",
            "Maíz", "Quinoa", "Cuscús", "Pan", "Uva", "Fresa", "Arándanos",
            "Kiwi", "Limon" -> 8

            // 11-12 meses -> pasta, verduras cocidas, quesos suaves, más variedad
            "Pasta", "Yogur natural", "Queso fresco" -> 10

            else -> 11
        }

        //SEGÚN LA IMAGEN DE QUE PUEDEN COMER LOS BEBES
        /*
        fun getRecipeTypes(): List<String> {
    val months = BabyUtils.getAgeInMonths()

    return when {
        months < 6 -> listOf() // solo leche, no recetas

        months in 6..7 -> listOf(
            "puree",
            "baby food",
            "porridge"
        )

        months in 8..9 -> listOf(
            "puree",
            "porridge",
            "soup",
            "mash",
            "baby food"
        )

        months in 10..12 -> listOf(
            "puree",
            "porridge",
            "soup",
            "mash",
            "finger food",
            "pancakes",
            "muffins"
        )

        else -> listOf(
            "main course",
            "side dish",
            "breakfast"
        )
    }
}
         */
    }
    fun getExcludedFoods(): List<String> {
        val months = getAgeInMonths() //sacamos los meses del bebé
//devuelve los alimento cuando el mes sea menor que la edad mínima para ese alimento
        return foodNames.filter { food ->
            months < getMinAgeForFood(food)
        }
    }
    fun getAllowedFoods(): List<String> {
        val months = getAgeInMonths()
//si ya tiene la edad devuelve los alimentos
        return foodNames.filter { food ->
            months >= getMinAgeForFood(food)
        }
    }
}
