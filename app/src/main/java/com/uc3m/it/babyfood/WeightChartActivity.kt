package com.uc3m.it.babyfood

import android.content.Context
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

//Uso OnChartValueSelectedListener para poder borrar los registros de la gráfica
class WeightChartActivity : AppCompatActivity(), OnChartValueSelectedListener {

    private lateinit var lineChart: LineChart
    private lateinit var dbAdapter: DatabaseAdapter
    private val dateLabels = ArrayList<String>() // Lista para guardar las fechas de las etiquetas
    private val weightIds = ArrayList<Long>() // Lista para guardar los IDs de la base de datos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_chart)

        lineChart = findViewById(R.id.weightChart)
        val btnBack = findViewById<Button>(R.id.btnBack) //boton de volver atrás

        dbAdapter = DatabaseAdapter(this)
        dbAdapter.open()

        setupChart() //cargamos configuraciones del gráfico
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
        lineChart.setScaleEnabled(true) //zoom
        lineChart.setPinchZoom(true) //permite hacer zoom con los dedos
        lineChart.setDrawGridBackground(false) // Sin fondo de cuadrícula

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
                    dateLabels[index] // devuelvo la fecha correspondiente al id del dato
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
        val weightEntries = ArrayList<Entry>()
        val minPercentileEntries = ArrayList<Entry>()
        val maxPercentileEntries = ArrayList<Entry>()
        val circleColors = ArrayList<Int>() // Lista para los colores de los puntos

        dateLabels.clear()
        weightIds.clear()

        val prefs = getSharedPreferences("BabyFoodPrefs", Context.MODE_PRIVATE)
        val birthDate = prefs.getString("fecha", "") ?: ""
        val isBoy = prefs.getString("genero", "") == "Niño"

        if (cursor.moveToFirst()) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault()) //parseo los datos para quedarme solo con el dia y el mes
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
                    weightEntries.add(Entry(index, weight.toFloat()))

                    // Cálculo de percentiles para este punto temporal
                    val monthsAtWeight = BabyUtils.getMonthsBetween(birthDate, dateStr)
                    val range = BabyUtils.getWeightRange(monthsAtWeight, isBoy)
                    minPercentileEntries.add(Entry(index, range.first))
                    maxPercentileEntries.add(Entry(index, range.second))
                    
                    // Si el peso está fuera de rango, el punto es ROJO, si no, VERDE o MORADO
                    if (weight < range.first || weight > range.second) {
                        circleColors.add(Color.RED)
                    } else {
                        circleColors.add(Color.parseColor("#6200EE")) // Morado original
                    }

                    index++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (weightEntries.isNotEmpty()) {
            val weightDataSet = LineDataSet(weightEntries, "Peso del Bebé (kg)").apply {
                color = Color.parseColor("#6200EE")
                setCircleColors(circleColors) // Aplicamos la lista de colores personalizada
                lineWidth = 3f
                circleRadius = 5f
                setDrawCircleHole(false)
                valueTextSize = 10f
            }
            // Línea de Percentil Mínimo (Referencia)
            val minDataSet = LineDataSet(minPercentileEntries, "P15 (Mínimo)").apply {
                color = Color.LTGRAY
                setDrawCircles(false)
                lineWidth = 1f
                enableDashedLine(10f, 10f, 0f)
                setDrawValues(false)
            }

            // Línea de Percentil Máximo (Referencia)
            val maxDataSet = LineDataSet(maxPercentileEntries, "P85 (Máximo)").apply {
                color = Color.LTGRAY
                setDrawCircles(false)
                lineWidth = 1f
                enableDashedLine(10f, 10f, 0f)
                setDrawValues(false)
            }

            val lineData = LineData(minDataSet, maxDataSet, weightDataSet)
            lineChart.data = lineData

            // fijo un número máximo de etiquetas en el eje X (5) para que sea siempre legible
            lineChart.xAxis.labelCount = if (dateLabels.size > 5) 5 else dateLabels.size
            lineChart.setVisibleXRangeMaximum(5f) //hace que la gráfica sea scrolleable a partir de 5 valores

            lineChart.animateX(1000) // Animación al cargar
            lineChart.invalidate()
        } else {
            lineChart.clear() // Limpia el gráfico si no hay datos
        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null || h == null) return

        // Comprobamos que el usuario ha pulsado sobre la línea de "Peso del Bebé" (es el dataset con índice 2)
        // ya que añadimos minDataSet (0), maxDataSet (1) y weightDataSet (2)
        if (h.dataSetIndex == 2) {
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
                            loadDataIntoChart()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    override fun onNothingSelected() {}

    override fun onDestroy() {
        super.onDestroy()
        dbAdapter.close()
    }
}