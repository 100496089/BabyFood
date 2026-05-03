package com.uc3m.it.babyfood

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.bottomnavigation.BottomNavigationView

class FoodRegisterActivity : AppCompatActivity(){

    private var dbAdapter: DatabaseAdapter? = null // sirve para manipular la BD
    private var m_listview: ListView? = null

    // Variables para mantener el estado del filtro
    private var currentSearchQuery: String = ""
    private var currentCategory: String? = null
    private var currentSort: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.foodregister_activity)

        //creamos el adaptador de la BD y la abrimos
        dbAdapter = DatabaseAdapter(this)
        dbAdapter!!.open()

        // Creamos un listview que va a contener el título de todas las notas y
        // en el que cuando pulsemos sobre un título lancemos una actividad de editar
        // la nota con el id correspondiente
        m_listview = findViewById<View>(R.id.id_list_view) as ListView
        m_listview!!.onItemClickListener =
            OnItemClickListener { arg0, view, position, id ->
                val i = Intent(
                    view.context,
                    AddFoodActivity::class.java
                )
                i.putExtra(DatabaseAdapter.KEY_ROWID, id)
                startActivityForResult(i, ACTIVITY_EDIT)
            }

        // rellenamos el listview con los títulos de todas las notas en la BD
        fillData()

        //buscador de notas
        //AYUDA DE GEMINI
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query ?: ""
                fillData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                fillData()
                return true
            }
        })

        //desplegable categorias
        val categories = resources.getStringArray(R.array.food_categories) //string de palabras
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)// como dibujar cada palabra en la lista
        val categoryDropdown = findViewById<AutoCompleteTextView>(R.id.category_search)// componente en el xml
        categoryDropdown.setAdapter(adapter)// le pasamos al adaptador la lista de palabras para que se llene
        // ESCUCHADOR PARA EL FILTRADO POR CATEGORÍA
        categoryDropdown.setOnItemClickListener { parent, view, position, id ->
            currentCategory = parent.getItemAtPosition(position).toString()
            currentCategory = if (currentCategory == "Todas") null else currentCategory
            categoryDropdown.setText(currentCategory, false)
            fillData()
        }
        // Desplegable ordenar por
        val sortOptions = resources.getStringArray(R.array.sort_by)
        val sortAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            sortOptions
        )

        val sortDropdown = findViewById<AutoCompleteTextView>(R.id.date_search)
        sortDropdown.setAdapter(sortAdapter)

        // Escuchador para ordenar
        sortDropdown.setOnItemClickListener { parent, _, position, _ ->
            currentSort = parent.getItemAtPosition(position).toString()
            sortDropdown.setText(currentSort, false)
            fillData()
        }
        //AYUDA DE GEMINI
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { view, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navInsets.bottom)
            insets
        }
        bottomNav.setOnItemSelectedListener { item ->

            when(item.itemId){

                R.id.home_button -> {
                    true
                }

                R.id.search_button -> {
                    //SACADO DE CLASE
                    // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
                    val intent = Intent(
                        this,
                        FoodActivity::class.java
                    )
                    startActivity(intent)
                    true
                }

                R.id.favorites_button -> {
                    // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
                    val intent = Intent(
                        this,
                        FavoritesActivity::class.java
                    )
                    startActivity(intent)
                    true
                }

                R.id.calendar_button -> {
                    // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
                    val intent = Intent(
                        this,
                        CalendarActivity::class.java
                    )
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }

    // boton back
    fun back(view: View?){
        val intent = Intent(
            this,
            HomeActivity::class.java
        )
        startActivity(intent)
    }
    fun createNote(view: View?){
        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
        val intent = Intent(
            this,
            AddFoodActivity::class.java
        )
        startActivity(intent)
    }

    private fun fillData() {
        val notesCursor = dbAdapter!!.fetchNotes(
            currentSearchQuery,
            currentCategory,
            currentSort
        )

        startManagingCursor(notesCursor)

        val from = arrayOf(
            DatabaseAdapter.KEY_NAME,
            DatabaseAdapter.KEY_COMMENT,
            DatabaseAdapter.KEY_DATE,
            DatabaseAdapter.KEY_PHOTO,
            DatabaseAdapter.KEY_RATE
        )

        val to = intArrayOf(
            R.id.name,
            R.id.comment,
            R.id.date,
            R.id.photo,
            R.id.rate
        )

        val adapter = SimpleCursorAdapter(
            this,
            R.layout.list_item_food,
            notesCursor,
            from,
            to,
            0
        )
        //Gemini
        adapter.viewBinder = SimpleCursorAdapter.ViewBinder { view, cursor, columnIndex ->
            if (view.id == R.id.rate) {
                val ratingValue = cursor.getString(columnIndex)
                val rating = ratingValue?.toFloatOrNull() ?: 0f
                val numStars = rating.toInt()
                // Crea un string con estrellas rellenas y vacías
                val stars = "★".repeat(numStars) + "☆".repeat(5 - numStars)
                (view as TextView).text = stars
                true // Indica que ya hemos gestionado nosotros esta vista
            } else {
                false // Deja que el adaptador maneje el resto (nombre, fecha, etc.)
            }
        }
        // ------------------------------

        m_listview!!.adapter = adapter
    }

    fun deleteNoteClick(view: View) {
        // Obtenemos la posición del elemento en la lista a través de su vista padre
        val position = m_listview!!.getPositionForView(view)

        // Obtenemos el ID de la base de datos de esa posición
        val id = m_listview!!.getItemIdAtPosition(position)

        // Mostramos un diálogo de confirmación
        val ad= com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
        ad.setTitle("Eliminar nota")
        ad.setMessage("¿Estás seguro de que quieres eliminar esta nota?")
        ad.setPositiveButton("Eliminar") { _, _ ->
            // 4. Llamamos al adaptador de la BD para borrar
            if (dbAdapter!!.deleteNote(id)) {
                    fillData() // 5. Recargamos la lista
                    Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // si se modifica una nota, o se añade, para que se actualice la lista
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        fillData()
    }

    companion object {
        private const val ACTIVITY_EDIT = 1
    }
}