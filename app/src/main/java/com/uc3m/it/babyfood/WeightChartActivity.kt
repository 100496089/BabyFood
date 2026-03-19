package com.uc3m.it.babyfood

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class WeightChartActivity : AppCompatActivity(), OnChartValueSelectedListener {

    private lateinit var lineChart: LineChart
    private lateinit var dbAdapter: DatabaseAdapter
    private val dateLabels = ArrayList<String>() // Lista para guardar las fechas de las etiquetas
    private val weightIds = ArrayList<Long>() // Lista para guardar los IDs de la base de datos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_chart)

        lineChart = findViewById(R.id.weightChart)
        val btnBack = findViewById<Button>(R.id.btnBack)

        dbAdapter = DatabaseAdapter(this)
        dbAdapter.open()

        setupChart()
        loadDataIntoChart()

        btnBack.setOnClickListener {
            finish()
        }
        
        lineChart.setOnChartValueSelectedListener(this)
    }

    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.setDrawGridBackground(false)

        // Configuración del Eje X
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // Forzar a que los saltos sean de 1 en 1 (un punto por etiqueta)
        
        // Formateador para convertir el índice (0, 1, 2...) de vuelta a fecha
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < dateLabels.size) {
                    dateLabels[index]
                } else {
                    ""
                }
            }
        }

        lineChart.axisRight.isEnabled = false
        val leftAxis = lineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = Color.BLACK
    }

    private fun loadDataIntoChart() {
        val cursor = dbAdapter.fetchAllWeights()
        val entries = ArrayList<Entry>()
        dateLabels.clear()
        weightIds.clear()

        if (cursor.moveToFirst()) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            var index = 0f

            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_WEIGHT_ID))
                val weight = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_WEIGHT_VALUE))
                val dateStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_WEIGHT_DATE))
                
                try {
                    val date = inputFormat.parse(dateStr)
                    val formattedDate = outputFormat.format(date!!)
                    
                    // Guardamos el ID y la fecha para la etiqueta y usamos el índice para la posición X
                    weightIds.add(id)
                    dateLabels.add(formattedDate)
                    entries.add(Entry(index, weight.toFloat()))
                    index++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Evolución del Peso (kg)")
            
            // Personalización estética
            dataSet.color = Color.parseColor("#6200EE") // Color de la línea
            dataSet.setCircleColor(Color.parseColor("#BB86FC")) // Color de los puntos
            dataSet.lineWidth = 3f
            dataSet.circleRadius = 5f
            dataSet.setDrawCircleHole(false)
            dataSet.valueTextSize = 12f
            dataSet.setDrawFilled(true)
            dataSet.fillAlpha = 50
            dataSet.fillColor = Color.parseColor("#6200EE")
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Línea suavizada (opcional)

            val lineData = LineData(dataSet)
            lineChart.data = lineData
            
            // Forzar que se vea el eje X correctamente con los nuevos labels
            lineChart.xAxis.labelCount = if (dateLabels.size > 5) 5 else dateLabels.size
            
            lineChart.animateX(1000) // Animación al cargar
            lineChart.invalidate()
        } else {
            lineChart.clear() // Limpia el gráfico si no hay datos
        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null) return

        val index = e.x.toInt()
        if (index >= 0 && index < weightIds.size) {
            val weightId = weightIds[index]
            val date = dateLabels[index]
            val weight = e.y

            AlertDialog.Builder(this)
                .setTitle("Borrar registro")
                .setMessage("¿Deseas eliminar el registro de peso de $weight kg del día $date?")
                .setPositiveButton("Borrar") { _, _ ->
                    if (dbAdapter.deleteWeight(weightId)) {
                        Toast.makeText(this, "Registro borrado", Toast.LENGTH_SHORT).show()
                        loadDataIntoChart() // Recargar el gráfico
                    } else {
                        Toast.makeText(this, "Error al borrar", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onNothingSelected() {
        // No se requiere acción
    }

    override fun onDestroy() {
        super.onDestroy()
        dbAdapter.close()
    }
}
