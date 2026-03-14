package com.uc3m.it.babyfood

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainMenuActivity : AppCompatActivity() {

    private lateinit var imageView: ShapeableImageView
    private var currentPhotoPath: String? = null
    private lateinit var dbAdapter: FoodRegisterAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            saveImageToInternalStorage(bitmap)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { saveImageToInternalStorage(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar DB
        dbAdapter = FoodRegisterAdapter(this)
        dbAdapter.open()

        val prefs = getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        val nombreGuardado = prefs.getString("nombre", "")
        val isEditing = intent.getBooleanExtra("isEditing", false)

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

        imageView = findViewById(R.id.imageView)
        val editTextNombre = findViewById<EditText>(R.id.editTextNombre)
        val editTextFecha = findViewById<EditText>(R.id.editTextFecha)
        val editTextPeso = findViewById<EditText>(R.id.editTextPeso)
        val autoCompleteAlergia = findViewById<AutoCompleteTextView>(R.id.autoCompleteAlergia)
        val buttonEnviar = findViewById<Button>(R.id.buttonMenu)

        currentPhotoPath = prefs.getString("foto_perfil", null)
        currentPhotoPath?.let {
            val file = File(it)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
            }
        }

        findViewById<FrameLayout>(R.id.frameLayoutImage).setOnClickListener {
            showImagePickerOptions()
        }

        editTextNombre.setText(nombreGuardado)
        editTextFecha.setText(prefs.getString("fecha", ""))
        editTextPeso.setText(prefs.getString("peso", ""))
        autoCompleteAlergia.setText(prefs.getString("alergia", ""), false)

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

        val alergias = arrayOf("Ninguna", "Lactosa", "Gluten", "Huevo", "Pescado", "Legumbres", "Frutas", "Soja", "Otros")
        val adapter = ArrayAdapter(this, R.layout.list_item, alergias)
        autoCompleteAlergia.setAdapter(adapter)

        buttonEnviar.setOnClickListener {
            val nombre = editTextNombre.text.toString()
            val fecha = editTextFecha.text.toString()
            val pesoStr = editTextPeso.text.toString()
            val alergia = autoCompleteAlergia.text.toString()

            if (nombre.isNotEmpty() && pesoStr.isNotEmpty()) {
                val pesoDouble = pesoStr.toDoubleOrNull() ?: 0.0
                
                // 1. Guardar en SharedPreferences (Datos actuales)
                val editor = prefs.edit()
                editor.putString("nombre", nombre)
                editor.putString("fecha", fecha)
                editor.putString("peso", pesoStr)
                editor.putString("alergia", alergia)
                editor.putString("foto_perfil", currentPhotoPath)
                editor.apply()

                // 2. Guardar en Base de Datos (Historial para la gráfica)
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                dbAdapter.insertWeight(pesoDouble, todayDate)

                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, rellena al menos el nombre y el peso", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbAdapter.close()
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Hacer foto", "Elegir de galería")
        AlertDialog.Builder(this)
            .setTitle("Cambiar foto del bebé")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePictureLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val fileName = "perfil_bebe_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()
            currentPhotoPath?.let { File(it).delete() }
            currentPhotoPath = file.absolutePath
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}