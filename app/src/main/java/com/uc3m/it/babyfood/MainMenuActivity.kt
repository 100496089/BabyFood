package com.uc3m.it.babyfood

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CARGAR DATOS GUARDADOS PARA COMPROBAR SI YA EXISTEN
        val prefs = getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        val nombreGuardado = prefs.getString("nombre", "")
        
        // Comprobar si venimos de "Editar Perfil"
        val isEditing = intent.getBooleanExtra("isEditing", false)

        // Redirigir a HomeActivity SOLO si ya hay datos Y NO estamos editando
        if (!nombreGuardado.isNullOrEmpty() && !isEditing) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a los componentes
        val editTextNombre = findViewById<EditText>(R.id.editTextNombre)
        val editTextFecha = findViewById<EditText>(R.id.editTextFecha)
        val editTextPeso = findViewById<EditText>(R.id.editTextPeso)
        val autoCompleteAlergia = findViewById<AutoCompleteTextView>(R.id.autoCompleteAlergia)
        val buttonEnviar = findViewById<Button>(R.id.buttonMenu)

        // Cargar datos en los campos para editar
        editTextNombre.setText(nombreGuardado)
        editTextFecha.setText(prefs.getString("fecha", ""))
        editTextPeso.setText(prefs.getString("peso", ""))
        autoCompleteAlergia.setText(prefs.getString("alergia", ""), false)

        // Configuración de Fecha (DatePicker)
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona fecha de nacimiento")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        editTextFecha.setOnClickListener {
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = Date(selection)
            editTextFecha.setText(sdf.format(date))
        }

        // Configuración de Alergias
        val alergias = arrayOf(
            "Ninguna",
            "Lactosa",
            "Gluten",
            "Huevo",
            "Pescado",
            "Legumbres",
            "Frutas",
            "Soja",
            "Otros"
        )
        val adapter = ArrayAdapter(this, R.layout.list_item, alergias)
        autoCompleteAlergia.setAdapter(adapter)

        // GUARDAR Y ENVIAR
        buttonEnviar.setOnClickListener {
            // Guardar los datos
            val editor = prefs.edit()
            editor.putString("nombre", editTextNombre.text.toString())
            editor.putString("fecha", editTextFecha.text.toString())
            editor.putString("peso", editTextPeso.text.toString())
            editor.putString("alergia", autoCompleteAlergia.text.toString())
            editor.apply()

            // Navegar a HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
