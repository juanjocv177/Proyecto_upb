package com.example.proyecto_upb

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_principal)

        val calcular = findViewById<Button>(R.id.btnCalcular)
        val historial = findViewById<Button>(R.id.btnHistorial)
        val salir = findViewById<Button>(R.id.btnSalir)

        calcular.setOnClickListener {
            val intent = Intent(this, CalculoActivity::class.java)
            startActivity(intent)
        }

        historial.setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }

        salir.setOnClickListener {

            val sesion = getSharedPreferences(
                "SesionFitBlood",
                MODE_PRIVATE
            )

            sesion.edit().remove("correo").apply()

            val intent = Intent(this, MainActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val sesion = getSharedPreferences(
            "SesionFitBlood",
            MODE_PRIVATE
        )

        val correo = sesion.getString("correo", "") ?: ""

        val textoCorreo = findViewById<TextView>(R.id.tvCorreoUsuario)
        val textoIMC = findViewById<TextView>(R.id.tvIMC)
        val textoCalorias = findViewById<TextView>(R.id.tvCalorias)
        val textoTotal = findViewById<TextView>(R.id.tvTotalCalculos)

        textoCorreo.text = correo

        if (correo.isEmpty()) {
            textoIMC.text = "--"
            textoCalorias.text = "--"
            textoTotal.text = "Cálculos realizados: 0"
            return
        }

        val datos = getSharedPreferences(
            "FitBlood_" + correo,
            MODE_PRIVATE
        )

        val imc = datos.getString("imc", "--")
        val calorias = datos.getInt("calorias", 0)
        val registros = datos.getString("historial", "") ?: ""

        textoIMC.text = imc

        if (calorias == 0) {
            textoCalorias.text = "--"
        } else {
            textoCalorias.text = calorias.toString()
        }

        var total = 0

        if (registros.isNotEmpty()) {
            total = registros.split("Fecha: ").size - 1
        }

        textoTotal.text = "Cálculos realizados: " + total
    }
}






