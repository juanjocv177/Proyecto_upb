package com.example.proyecto_upb

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalculoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculo)

        val peso = findViewById<EditText>(R.id.etPeso)
        val estatura = findViewById<EditText>(R.id.etEstatura)
        val edad = findViewById<EditText>(R.id.etEdad)

        val sexo = findViewById<Spinner>(R.id.spSexo)
        val actividad = findViewById<Spinner>(R.id.spActividad)

        val calcular = findViewById<Button>(R.id.btnCalcularResultado)
        val volver = findViewById<Button>(R.id.btnVolver)

        val resultadoIMC = findViewById<TextView>(R.id.tvResultadoIMC)
        val resultadoCalorias = findViewById<TextView>(R.id.tvResultadoCalorias)

        val opcionesSexo = arrayOf(
            "Selecciona tu sexo",
            "Masculino",
            "Femenino"
        )

        val opcionesActividad = arrayOf(
            "Selecciona tu actividad",
            "Sedentario",
            "Actividad ligera",
            "Actividad moderada",
            "Actividad intensa",
            "Actividad muy intensa"
        )

        val adaptadorSexo = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesSexo
        )

        adaptadorSexo.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        sexo.adapter = adaptadorSexo

        val adaptadorActividad = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opcionesActividad
        )

        adaptadorActividad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        actividad.adapter = adaptadorActividad

        calcular.setOnClickListener {

            val p = peso.text.toString().toDoubleOrNull()
            val e = estatura.text.toString().toDoubleOrNull()
            val a = edad.text.toString().toIntOrNull()

            if (p == null || e == null || a == null ||
                p <= 0 || e <= 0 || a <= 0 ||
                sexo.selectedItemPosition == 0 ||
                actividad.selectedItemPosition == 0) {

                Toast.makeText(
                    this,
                    "Completa todos los datos correctamente",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                val metros = e / 100
                val imc = p / (metros * metros)

                val metabolismo: Double

                if (sexo.selectedItemPosition == 1) {
                    metabolismo = 10 * p + 6.25 * e - 5 * a + 5
                } else {
                    metabolismo = 10 * p + 6.25 * e - 5 * a - 161
                }

                val factor: Double

                if (actividad.selectedItemPosition == 1) {
                    factor = 1.2
                } else if (actividad.selectedItemPosition == 2) {
                    factor = 1.375
                } else if (actividad.selectedItemPosition == 3) {
                    factor = 1.55
                } else if (actividad.selectedItemPosition == 4) {
                    factor = 1.725
                } else {
                    factor = 1.9
                }

                val calorias = metabolismo * factor

                val imcTexto = String.format(
                    Locale.US,
                    "%.2f",
                    imc
                )

                resultadoIMC.text = "IMC: " + imcTexto

                resultadoCalorias.text =
                    "Calorías diarias: " +
                            calorias.toInt() + " kcal"

                val sesion = getSharedPreferences(
                    "SesionFitBlood",
                    MODE_PRIVATE
                )

                val correo = sesion.getString("correo", "") ?: ""

                if (correo.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Debes iniciar sesión",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                    val datos = getSharedPreferences(
                        "FitBlood_" + correo,
                        MODE_PRIVATE
                    )

                    val historialAnterior = datos.getString(
                        "historial",
                        ""
                    ) ?: ""

                    val fecha = SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Date())

                    val nuevoRegistro =
                        "Fecha: " + fecha +
                                "\nIMC: " + imcTexto +
                                "\nCalorías diarias: " +
                                calorias.toInt() + " kcal" +
                                "\n----------------------------\n\n"

                    datos.edit()
                        .putString("imc", imcTexto)
                        .putInt("calorias", calorias.toInt())
                        .putString(
                            "historial",
                            nuevoRegistro + historialAnterior
                        )
                        .apply()

                    Toast.makeText(
                        this,
                        "Cálculo realizado y guardado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        volver.setOnClickListener {
            finish()
        }
    }
}




