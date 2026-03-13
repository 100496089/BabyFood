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

class FoodRegisterActivity : AppCompatActivity(){

    private var dbAdapter: FoodRegisterAdapter? = null // sirve para manipular la BD
    private var m_listview: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.foodregister_activity)

        //creamos el adaptador de la BD y la abrimos
        dbAdapter = FoodRegisterAdapter(this)
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
                i.putExtra(FoodRegisterAdapter.KEY_ROWID, id)
                startActivityForResult(i, ACTIVITY_EDIT)
            }

        // rellenamos el listview con los títulos de todas las notas en la BD
        fillData()
    }

    //BOTONES
    //Boton de Añadir alimento
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

        val from = arrayOf(FoodRegisterAdapter.KEY_NAME, FoodRegisterAdapter.KEY_COMMENT, FoodRegisterAdapter.KEY_DATE, FoodRegisterAdapter.KEY_PHOTO) //que columnas quieres mostrar
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

    // si se modifica una nota, o se añade, para que se actualice la lista
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        fillData()
    }

    //Boton de home
    fun home(view: View?) {
        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
        val intent = Intent(
            this,
            HomeActivity::class.java
        )
        startActivity(intent)
    }
    //boton buscar
    fun search(view: View?) {
        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
        val intent = Intent(
            this,
            FoodActivity::class.java
        )
        startActivity(intent)
    }

    //boton calendario
    fun calendar(view: View?){
        // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
        val intent = Intent(
            this,
            CalendarActivity::class.java
        )
        startActivity(intent)

    }

    companion object {
        private const val ACTIVITY_EDIT = 1
    }
}