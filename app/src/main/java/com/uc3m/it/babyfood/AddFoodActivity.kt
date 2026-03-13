package com.uc3m.it.babyfood

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date



class AddFoodActivity : AppCompatActivity(){

    private var ubicacion: File? = null // File que apunta a la ubicación de la imagen tomada

    private var NameText: EditText? = null
    private var commentText: EditText? = null
    private var mRowId: Long? = null
    private var dbAdapter: FoodRegisterAdapter? = null

    private var dateText: TextView? = null // Para mostrar la fecha seleccionada
    private var selectedDate: String = ""  // Para guardar el valor que irá a la BD



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.addfood_activity)

        // obtiene referencia a los views que componen el layout
        NameText = findViewById<View>(R.id.Name) as EditText
        commentText = findViewById<View>(R.id.Comment) as EditText
        dateText = findViewById<View>(R.id.Calendar) as TextView

        //creamos el adaptador de la BD y la abrimos
        dbAdapter = FoodRegisterAdapter(this)
        dbAdapter!!.open() // abre la base de datos

        // obtiene id de fila de la tabla si se le ha pasado (hemos pulsado una nota para editarla)
        mRowId = if ((savedInstanceState == null)) null else savedInstanceState.getSerializable( // Si mRowId es null → estás creando una comida/note nueva.
            FoodRegisterAdapter.KEY_ROWID
        ) as Long?
        if (mRowId == null) {
            val extras = intent.extras
            mRowId = extras?.getLong(FoodRegisterAdapter.KEY_ROWID) // busca en un sitio el numero que tenga el id (KEY_ID)
        }

        if (mRowId != null) { // si hay id es que lo que estamos haciendo es mdificar un registro, por eso fetchNote
            val note = dbAdapter!!.fetchNote(mRowId!!)
            NameText!!.setText(
                note.getString(
                    note.getColumnIndexOrThrow(FoodRegisterAdapter.KEY_NAME) // El código lee el nombre de un alimento desde la base de datos y lo muestra en el TextView
                )
            )
            commentText!!.setText(
                note.getString(
                    note.getColumnIndexOrThrow(FoodRegisterAdapter.KEY_COMMENT)
                )
            )
            dateText!!.setText(
                note.getString(
                    note.getColumnIndexOrThrow(FoodRegisterAdapter.KEY_DATE)
                )
            )
        }
    }


    // almacenar la comida
    //guardar en la BD
    fun saveNote(view: View?) {
        val name = NameText!!.text.toString()
        val comment = commentText!!.text.toString()
        val date= dateText!!.text.toString()
        val imagePath = ubicacion?.path ?: ""


        if (mRowId == null) {
            val id = dbAdapter!!.createNote(name, comment, date, imagePath) // si antes no habia la crea
            if (id > 0) {
                mRowId = id
            }
        } else {
            dbAdapter!!.updateNote(mRowId!!, name, comment, date, imagePath) // y si no la
        }
        setResult(RESULT_OK)
        dbAdapter!!.close()
        val intent = Intent(
            this,
            FoodRegisterActivity::class.java
        )
        startActivity(intent)
    }
    fun date(view: View) {
        val c = java.util.Calendar.getInstance()
        val year = c.get(java.util.Calendar.YEAR)
        val month = c.get(java.util.Calendar.MONTH)
        val day = c.get(java.util.Calendar.DAY_OF_MONTH)

        val dpd = android.app.DatePickerDialog(this, { _, yearSel, monthSel, daySel ->
            // Formateamos la fecha (mes + 1 porque en Java/Kotlin los meses van de 0 a 11)
            selectedDate = "$daySel/${monthSel + 1}/$yearSel"
            dateText?.text = "${this.selectedDate}"
        }, year, month, day)

        dpd.show()
    }

    // boton back
    fun back(view: View?){
        finish()
    }

    //sacar foto

    // En caso de que la activity sea destruida mientras que otra app toma la fotografía,
    // es necesario guardar la ruta y restaurarla de nuevo, puesto que si no, no habrá forma
    // de acceder a la imagen.
    // esta funcion guarda la ruta en el Bundle la ruta del archivo de la imagen
    public override fun onSaveInstanceState(outState: Bundle) {
        if (ubicacion != null) {
            outState.putString(CLAVE_RUTA_IMAGEN, ubicacion!!.path) // ubicacion.path: ruta del archivo que se guarda en el Bundle con la clave CLAVE_RUTA_IMAGEN
        }
        super.onSaveInstanceState(outState)
    }

    //“Si en el Bundle hay una ruta guardada, recupérala y reconstruye ubicacion.”
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (savedInstanceState.containsKey(CLAVE_RUTA_IMAGEN)) {
            ubicacion = File(savedInstanceState.getString(CLAVE_RUTA_IMAGEN).toString())
        }
        super.onRestoreInstanceState(savedInstanceState)
    }

    //FUNCION PARA LANZAR LA APP DE CAMARA
    fun camera(v: View) {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)// abre una app para sacar una foto

        // creamos una ruta en la que guardar la imagen
        ubicacion = obtenerUbicacionImagen() // objeto File

        if (ubicacion != null) { // comprueba que haya almacenamiento disponible, es decir, si Si obtenerUbicacionImagen() devolvió null, significa que no se puede usar el almacenamiento.
            val photoURI = FileProvider.getUriForFile( // crea una URI segura
                this, "com.uc3m.it.babyfood.provider",
                ubicacion!!
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) //Esto le da permisos temporales a la app de cámara para: leer el archivo y escribir la foto en esa ruta
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)// paso la ruta de la foto a la app de camara, le digo donde quiero que guarde la foto
            startActivityForResult(intent,CODIGO_HACER_FOTO) //lanza la camara
        } else {
            Toast.makeText(
                this, "No se puede acceder al sistema de archivos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Creamos un objeto File (que no es otra cosa que una ruta) para guardar ahí la foto  */
    //Crear la ruta del archivo en almacenamiento externo privado de la app
    private fun obtenerUbicacionImagen(): File? {

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) return null //comprueba si el almacenamiento externo está montado y escribible (MEDIA_MOUNTED).
        //directorio privado de la app en externo
        val directorioExterno =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Si aún no existe el directorio, lo creamos
        if (!directorioExterno!!.exists()) {
            if (!directorioExterno.mkdirs()) {
                Log.d("BabyFood", "no se puede crear el directorio")
                return null
            }
        }

        // crea un nombre unico con timestamp
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fichero = File(directorioExterno.path + File.separator + "IMG_" + timestamp + ".jpg") // define donde se va a guardar la foto

        // Devolvemos el objeto File
        return fichero
    }
    //Esta función se ejecuta cuando la cámara termina y vuelve a tu app
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(
            "onActivityResult",
            "requestCode = " + requestCode + ", resultCode = " + resultCode + ", data = " + (if (data == null) "null" else "not null")
        )

        if (requestCode == com.uc3m.it.babyfood.AddFoodActivity.Companion.CODIGO_HACER_FOTO) {
            if (resultCode != RESULT_CANCELED) {
                // La imagen ha sido capturada y grabada en la ubicación indicada
                Toast.makeText(this, "Foto capturada correctamente", Toast.LENGTH_SHORT).show()
            } else {
                // La captura ha sido cancelada por el usuario, ha fallado,
                // o la otra app se cerró inesperadamente
            }
        }
    }

    companion object {
        // código de acción para lanzar un Intent que solicite una captura
        private const val CODIGO_HACER_FOTO = 100

        // Clave para no perder la ruta en caso de destrucción de la activity
        private const val CLAVE_RUTA_IMAGEN = "CLAVE_RUTA_IMAGEN"
    }

}


