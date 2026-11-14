package com.example.proyecto_upb

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val correo = findViewById<EditText>(R.id.etCorreo)
        val clave = findViewById<EditText>(R.id.etClave)

        val ingresar = findViewById<Button>(R.id.btnIngresar)
        val registrar = findViewById<Button>(R.id.btnRegistrar)
        val estado = findViewById<TextView>(R.id.tvEstado)

        val entrar = findViewById<Button>(R.id.btnEntrar)

        ingresar.setOnClickListener {

            if (correo.text.isEmpty() || clave.text.isEmpty()) {
                estado.text = "Completa todos los campos"
            } else {

                val datos = getSharedPreferences(
                    "SesionFitBlood",
                    MODE_PRIVATE
                )

                datos.edit()
                    .putString(
                        "correo",
                        correo.text.toString().trim().lowercase()
                    )
                    .apply()

                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }
        }

        entrar.setOnClickListener {

            if (correo.text.isEmpty() || clave.text.isEmpty()) {
                estado.text = "Completa todos los campos"
            } else {

                val datos = getSharedPreferences(
                    "SesionFitBlood",
                    MODE_PRIVATE
                )

                datos.edit()
                    .putString(
                        "correo",
                        correo.text.toString().trim().lowercase()
                    )
                    .apply()

                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }
        }

        registrar.setOnClickListener {
            estado.text = "Crear cuenta"
        }
    }
}







