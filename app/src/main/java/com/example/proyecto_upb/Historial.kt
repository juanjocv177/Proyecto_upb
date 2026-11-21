package com.example.proyecto_upb

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class RegistroFitBlood(
    val fecha: Date,
    val imc: Float,
    val calorias: Int
)

class HistorialActivity : AppCompatActivity() {

    private lateinit var contenedorGrafica: FrameLayout
    private lateinit var contenedorRegistros: LinearLayout
    private lateinit var resumen: TextView
    private lateinit var total: TextView

    private lateinit var tresMeses: Button
    private lateinit var esteAnio: Button
    private lateinit var todo: Button

    private var registros = mutableListOf<RegistroFitBlood>()
    private var filtro = 1

    private val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial)

        contenedorGrafica = findViewById(R.id.contenedorGrafica)
        contenedorRegistros = findViewById(R.id.contenedorRegistros)
        resumen = findViewById(R.id.tvResumenIMC)
        total = findViewById(R.id.tvTotalRegistros)

        tresMeses = findViewById(R.id.btnTresMeses)
        esteAnio = findViewById(R.id.btnEsteAnio)
        todo = findViewById(R.id.btnTodo)

        val eliminar = findViewById<Button>(R.id.btnEliminarHistorial)
        val volver = findViewById<Button>(R.id.btnVolverHistorial)

        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            eliminar.isEnabled = false
            resumen.text = "Debes iniciar sesión"
        } else {
            cargarRegistros(usuario.uid)
        }

        tresMeses.setOnClickListener {
            filtro = 1
            mostrarDatos()
        }

        esteAnio.setOnClickListener {
            filtro = 2
            mostrarDatos()
        }

        todo.setOnClickListener {
            filtro = 3
            mostrarDatos()
        }

        eliminar.setOnClickListener {

            if (usuario != null) {

                AlertDialog.Builder(this)
                    .setTitle("Eliminar historial")
                    .setMessage("¿Deseas eliminar todos tus cálculos?")
                    .setPositiveButton("Eliminar") { _, _ ->

                        val referencia = FirebaseDatabase.getInstance()
                            .getReference("usuarios")
                            .child(usuario.uid)
                            .child("calculos")

                        referencia.removeValue()
                            .addOnSuccessListener {

                                val correo = usuario.email ?: ""

                                val datos = getSharedPreferences(
                                    "FitBlood_" + correo,
                                    MODE_PRIVATE
                                )

                                datos.edit()
                                    .remove("historial")
                                    .remove("imc")
                                    .remove("calorias")
                                    .apply()

                                registros.clear()
                                mostrarDatos()

                                Toast.makeText(
                                    this,
                                    "Historial eliminado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Error al eliminar: " + it.localizedMessage,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        volver.setOnClickListener {
            finish()
        }

        mostrarDatos()
    }

    private fun cargarRegistros(uid: String) {

        val referencia = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)
            .child("calculos")

        referencia.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                registros.clear()

                for (dato in snapshot.children) {

                    val imc = dato.child("imc")
                        .getValue(Double::class.java)

                    val calorias = dato.child("calorias")
                        .getValue(Int::class.java)

                    val fechaMillis = dato.child("fechaMillis")
                        .getValue(Long::class.java)

                    if (imc != null &&
                        calorias != null &&
                        fechaMillis != null) {

                        registros.add(
                            RegistroFitBlood(
                                Date(fechaMillis),
                                imc.toFloat(),
                                calorias
                            )
                        )
                    }
                }

                registros.sortBy { it.fecha.time }
                mostrarDatos()
            }

            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(
                    this@HistorialActivity,
                    "Error al cargar historial: " + error.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun mostrarDatos() {

        tresMeses.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (filtro == 1) Color.RED else Color.DKGRAY
            )

        esteAnio.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (filtro == 2) Color.RED else Color.DKGRAY
            )

        todo.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (filtro == 3) Color.RED else Color.DKGRAY
            )

        val calendario = Calendar.getInstance()

        val fechaLimite = when (filtro) {

            1 -> {
                calendario.add(Calendar.MONTH, -3)
                calendario.time
            }

            2 -> {
                calendario.set(Calendar.MONTH, Calendar.JANUARY)
                calendario.set(Calendar.DAY_OF_MONTH, 1)
                calendario.set(Calendar.HOUR_OF_DAY, 0)
                calendario.set(Calendar.MINUTE, 0)
                calendario.set(Calendar.SECOND, 0)
                calendario.set(Calendar.MILLISECOND, 0)
                calendario.time
            }

            else -> Date(0)
        }

        val ahora = Date()

        val filtrados = registros.filter {
            !it.fecha.before(fechaLimite) &&
                    !it.fecha.after(ahora)
        }.sortedBy { it.fecha.time }

        total.text = filtrados.size.toString() + " registros"

        if (filtrados.isEmpty()) {
            resumen.text = "No hay cálculos en este período"
        } else {

            val ultimo = filtrados.last()

            resumen.text = "Último IMC: " +
                    String.format(Locale.US, "%.2f", ultimo.imc)
        }

        contenedorGrafica.removeAllViews()

        val grafica = GraficaIMC(this, filtrados)
        contenedorGrafica.addView(grafica)

        contenedorRegistros.removeAllViews()

        if (filtrados.isEmpty()) {

            val mensaje = TextView(this)
            mensaje.text = "Todavía no tienes registros en este período."
            mensaje.textSize = 16f
            mensaje.setTextColor(Color.DKGRAY)
            mensaje.setPadding(15, 25, 15, 25)

            contenedorRegistros.addView(mensaje)

        } else {

            for (registro in filtrados.reversed()) {

                val tarjeta = LinearLayout(this)
                tarjeta.orientation = LinearLayout.VERTICAL
                tarjeta.setPadding(20, 20, 20, 20)
                tarjeta.setBackgroundColor(Color.rgb(244, 244, 244))

                val titulo = TextView(this)
                titulo.text = formato.format(registro.fecha)
                titulo.textSize = 17f
                titulo.setTextColor(Color.BLACK)
                titulo.setTypeface(null, 1)

                val imc = TextView(this)
                imc.text = "IMC: " +
                        String.format(Locale.US, "%.2f", registro.imc)
                imc.textSize = 16f
                imc.setTextColor(Color.rgb(233, 32, 32))
                imc.setPadding(0, 12, 0, 5)

                val calorias = TextView(this)
                calorias.text = "Calorías diarias: " +
                        registro.calorias + " kcal"
                calorias.textSize = 15f
                calorias.setTextColor(Color.DKGRAY)

                tarjeta.addView(titulo)
                tarjeta.addView(imc)
                tarjeta.addView(calorias)

                val parametros = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                parametros.bottomMargin = 15

                contenedorRegistros.addView(tarjeta, parametros)
            }
        }
    }
}

class GraficaIMC(
    context: Context,
    private val datos: List<RegistroFitBlood>
) : View(context) {

    private val pintura = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val ancho = width.toFloat()
        val alto = height.toFloat()

        val izquierda = 55f
        val derecha = ancho - 25f
        val arriba = 30f
        val abajo = alto - 45f

        pintura.strokeWidth = 2f
        pintura.color = Color.LTGRAY

        for (i in 0..4) {
            val y = arriba + (abajo - arriba) * i / 4
            canvas.drawLine(izquierda, y, derecha, y, pintura)
        }

        if (datos.isEmpty()) {

            pintura.color = Color.GRAY
            pintura.textSize = 28f
            pintura.textAlign = Paint.Align.CENTER

            canvas.drawText(
                "Sin datos para graficar",
                ancho / 2,
                alto / 2,
                pintura
            )

            return
        }

        val minimo = datos.minOf { it.imc }
        val maximo = datos.maxOf { it.imc }

        val rango = if (maximo == minimo) 2f else maximo - minimo
        val base = if (maximo == minimo) minimo - 1f else minimo

        pintura.textSize = 24f
        pintura.textAlign = Paint.Align.RIGHT
        pintura.color = Color.GRAY

        for (i in 0..4) {

            val valor = base + rango * (4 - i) / 4
            val y = arriba + (abajo - arriba) * i / 4

            canvas.drawText(
                String.format(Locale.US, "%.1f", valor),
                izquierda - 8,
                y + 8,
                pintura
            )
        }

        val puntos = mutableListOf<Pair<Float, Float>>()

        for (i in datos.indices) {

            val x = if (datos.size == 1) {
                (izquierda + derecha) / 2
            } else {
                izquierda +
                        (derecha - izquierda) * i / (datos.size - 1)
            }

            val y = abajo -
                    (datos[i].imc - base) / rango * (abajo - arriba)

            puntos.add(Pair(x, y))
        }

        pintura.color = Color.rgb(233, 32, 32)
        pintura.strokeWidth = 5f

        for (i in 0 until puntos.size - 1) {

            canvas.drawLine(
                puntos[i].first,
                puntos[i].second,
                puntos[i + 1].first,
                puntos[i + 1].second,
                pintura
            )
        }

        pintura.style = Paint.Style.FILL

        for (punto in puntos) {
            canvas.drawCircle(
                punto.first,
                punto.second,
                6f,
                pintura
            )
        }

        val formatoFecha = SimpleDateFormat(
            "dd/MM",
            Locale.getDefault()
        )

        pintura.color = Color.GRAY
        pintura.textSize = 24f
        pintura.textAlign = Paint.Align.CENTER

        canvas.drawText(
            formatoFecha.format(datos.first().fecha),
            izquierda,
            alto - 10f,
            pintura
        )

        if (datos.size > 1) {
            canvas.drawText(
                formatoFecha.format(datos.last().fecha),
                derecha,
                alto - 10f,
                pintura
            )
        }
    }
}



