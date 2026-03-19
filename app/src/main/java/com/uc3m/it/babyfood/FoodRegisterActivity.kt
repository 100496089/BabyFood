package com.uc3m.it.babyfood

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView.OnItemClickListener
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView

class FoodRegisterActivity : AppCompatActivity(){

    private var dbAdapter: DatabaseAdapter? = null // sirve para manipular la BD
    private var m_listview: ListView? = null

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
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Cada vez que escribas, filtramos la lista
                val cursor = dbAdapter!!.fetchNotesBySearch(newText ?: "")
                // Actualizamos el cursor del adaptador que ya tienes
                (m_listview?.adapter as? SimpleCursorAdapter)?.changeCursor(cursor)
                return true
            }
        })

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->

            when(item.itemId){

                R.id.home_button -> {
                    true
                }

                R.id.search_button -> {
                    // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
                    val intent = Intent(
                        this,
                        FoodActivity::class.java
                    )
                    startActivity(intent)
                    true
                }

                R.id.favorites_button -> {
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

    private fun fillData() { // rellenamos el listview con los títulos de todas las notas
        val notesCursor = dbAdapter!!.fetchAllNotes() //puntero de todas las notas
        startManagingCursor(notesCursor)

        val from = arrayOf(DatabaseAdapter.KEY_NAME, DatabaseAdapter.KEY_COMMENT, DatabaseAdapter.KEY_DATE,
            DatabaseAdapter.KEY_PHOTO, DatabaseAdapter.KEY_RATE) //que columnas quieres mostrar
        val to = intArrayOf(R.id.name, R.id.comment, R.id.date, R.id.photo) //a que vistas del diseño van

        val adapter = SimpleCursorAdapter( // recorre cada fila de notesCursor y la muestra en el listview
            this,
            R.layout.list_item_food,
            notesCursor,
            from,
            to,
            0
        )

        m_listview!!.adapter = adapter
    }

    fun deleteNoteClick(view: View) {
        // 1. Obtenemos la posición del elemento en la lista a través de su vista padre
        val position = m_listview!!.getPositionForView(view)

        // 2. Obtenemos el ID de la base de datos de esa posición
        val id = m_listview!!.getItemIdAtPosition(position)

        // 3. Mostramos un diálogo de confirmación
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