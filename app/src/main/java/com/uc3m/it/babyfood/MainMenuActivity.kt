package com.uc3m.it.babyfood

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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

    private lateinit var imageViewProfile: ShapeableImageView
    private var currentPhotoPath: String? = null
    private lateinit var dbAdapter: DatabaseAdapter

    //Esto permite abrir la galería del usuario
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it) // Abre el flujo de entrada de la imagen a través de la URI
            val bitmap = BitmapFactory.decodeStream(inputStream) // Convierte el flujo de entrada en un Bitmap
            saveImageToInternalStorage(bitmap)
        }
    }

    //Usamos TakePicturePreview() porque estamos trabajando con un thumbnail y me devuelve un bitmap directamente
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let { saveImageToInternalStorage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar DB
        dbAdapter = DatabaseAdapter(this)
        dbAdapter.open()

        val prefs = getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        //Leo los datos guardados
        val nombreGuardado = prefs.getString("nombre", "")
        val fechaGuardada = prefs.getString("fecha", null)
        val pesoGuardado = prefs.getString("peso", null)
        val fotoGuardada = prefs.getString("foto_perfil", null)

        val isEditing = intent.getBooleanExtra("isEditing", false) //Compruebo si estoy editando el perfil

        // Comprobar si el perfil está completo
        val perfilCompleto = !nombreGuardado.isNullOrEmpty() &&
                !fechaGuardada.isNullOrEmpty() &&
                !pesoGuardado.isNullOrEmpty() &&
                !fotoGuardada.isNullOrEmpty()

        // Si el perfil está completo y no estamos editando, saltar a HomeActivity
        if (perfilCompleto && !isEditing) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge() //Quito los márgenes
        setContentView(R.layout.activity_main_menu) //Utilizo el layout de activity_main_menu

        //Como estoy quitando los márgenes, para evitar que los elementos queden tapados,
        //calculo el tamaño de las barras del sistema y lo uso como padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageViewProfile = findViewById(R.id.imageViewProfile)
        val editTextNombre = findViewById<EditText>(R.id.editTextNombre)
        val editTextFecha = findViewById<EditText>(R.id.editTextFecha)
        val editTextPeso = findViewById<EditText>(R.id.editTextPeso)
        val autoCompleteAlergia = findViewById<AutoCompleteTextView>(R.id.autoCompleteAlergia)
        val buttonEnviar = findViewById<Button>(R.id.buttonMenu)

        currentPhotoPath = prefs.getString("foto_perfil", null)
        currentPhotoPath?.let {
            val file = File(it)
            if (file.exists()) {
                imageViewProfile.setImageURI(Uri.fromFile(file))
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

            // Comprobamos los campos actuales, no los guardados anteriormente (no usamos perfilCompleto)
            val camposRellenos = nombre.isNotEmpty() &&
                    fecha.isNotEmpty() &&
                    pesoStr.isNotEmpty() &&
                    !currentPhotoPath.isNullOrEmpty()

            if (camposRellenos) {
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
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
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
                    0 -> takePictureLauncher.launch(null) //takePicturePreview no necesita input
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val fileName = "perfil_bebe_${System.currentTimeMillis()}.jpg" //Genero un nombre para la imagen guardada con el tiempo en milisegundos para no sobreescribir fotos
        val file = File(filesDir, fileName) //Guardo la foto en el almacenamiento interno del dispositivo
        try {
            val fos = FileOutputStream(file) //recojo el stream de datos (los bytes) de la imagen
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos) //convierto de bitmap a jpeg
            fos.close() //cierro el stream de datos
            currentPhotoPath?.let { File(it).delete() }//si ya había una foto, la borro
            currentPhotoPath = file.absolutePath //guardo el path de la foto actual
            imageViewProfile.setImageBitmap(bitmap) //actualizo la foto
        } catch (e: Exception) {
            e.printStackTrace() //imprime el error en el log; impide que la app se pare si ha habido algún error
        }
    }
}
