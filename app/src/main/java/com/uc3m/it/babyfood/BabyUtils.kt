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
    fun getMinAgeForFood(food: String): Int {
        return when (food) {

            // 6 meses -> papillas suaves de frutas y verduras
            "Zanahoria", "Brócoli", "Patata", "Tomate", "Calabaza", "Calabacín",
            "Berenjena", "Guisantes", "Judías verdes", "Puerro", "Batata",
            "Coliflor", "Pepino",
            "Plátano", "Manzana", "Pera", "Naranja", "Aguacate", "Mango",
            "Papaya", "Ciruela", "Melocotón", "Sandía", "Melón",
            "Arroz", "Leche", "Aceite de oliva" -> 6

            // 7-8 meses -> carnes, huevo, papillas más espesas
            "Pollo", "Vacuno", "Cerdo", "Pavo", "Conejo", "Huevo",
            "Avena" -> 7

            // 9-10 meses -> alimentos blandos en trozos
            "Merluza", "Salmón", "Lentejas", "Garbanzos", "Alubias", "Tofu",
            "Maíz", "Quinoa", "Cuscús", "Pan", "Uva", "Fresa", "Arándanos",
            "Kiwi", "Limon" -> 9

            // 11-12 meses -> pasta, verduras cocidas, quesos suaves, más variedad
            "Pasta", "Yogur natural", "Queso fresco" -> 11

            else -> 12
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
