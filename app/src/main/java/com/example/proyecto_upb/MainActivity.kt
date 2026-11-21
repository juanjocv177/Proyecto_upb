package com.example.proyecto_upb

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nombre = findViewById<EditText>(R.id.etNombre)
        val correo = findViewById<EditText>(R.id.etCorreo)
        val clave = findViewById<EditText>(R.id.etClave)

        val ingresar = findViewById<Button>(R.id.btnIngresar)
        val registrar = findViewById<Button>(R.id.btnRegistrar)
        val entrar = findViewById<Button>(R.id.btnEntrar)
        val estado = findViewById<TextView>(R.id.tvEstado)

        val contenedorNombre =
            findViewById<LinearLayout>(R.id.contenedorNombre)

        val auth = FirebaseAuth.getInstance()

        val baseDatos = FirebaseDatabase.getInstance()
            .getReference("usuarios")

        var modoRegistro = false

        ingresar.setOnClickListener {
            modoRegistro = false
            contenedorNombre.visibility = View.GONE
            entrar.text = "Iniciar sesión"
            estado.text = ""
        }

        registrar.setOnClickListener {
            modoRegistro = true
            contenedorNombre.visibility = View.VISIBLE
            entrar.text = "Crear cuenta"
            estado.text = ""
        }

        entrar.setOnClickListener {

            val nombreUsuario = nombre.text.toString().trim()
            val correoUsuario = correo.text.toString().trim().lowercase()
            val claveUsuario = clave.text.toString()

            if (correoUsuario.isEmpty() || claveUsuario.isEmpty()) {

                estado.text = "Completa todos los campos"

            } else if (modoRegistro) {

                if (nombreUsuario.isEmpty()) {

                    estado.text = "Escribe tu nombre"

                } else if (claveUsuario.length < 6) {

                    estado.text =
                        "La contraseña debe tener mínimo 6 caracteres"

                } else {

                    estado.text = "Creando cuenta..."

                    auth.createUserWithEmailAndPassword(
                        correoUsuario,
                        claveUsuario
                    ).addOnCompleteListener(this) { tarea ->

                        if (tarea.isSuccessful) {

                            val usuario = auth.currentUser

                            if (usuario != null) {

                                val datosUsuario = hashMapOf<String, Any>(
                                    "nombre" to nombreUsuario,
                                    "correo" to correoUsuario
                                )

                                baseDatos.child(usuario.uid)
                                    .updateChildren(datosUsuario)
                                    .addOnSuccessListener {

                                        guardarSesion(correoUsuario)
                                        abrirMenu(correoUsuario)

                                    }
                                    .addOnFailureListener {

                                        estado.text =
                                            "Cuenta creada, pero no se guardó el perfil: " +
                                                    it.localizedMessage

                                    }

                            } else {

                                estado.text =
                                    "No se pudo obtener el usuario"

                            }

                        } else {

                            estado.text = "Error: " +
                                    (tarea.exception?.localizedMessage
                                        ?: "No se pudo crear la cuenta")

                        }
                    }
                }

            } else {

                estado.text = "Iniciando sesión..."

                auth.signInWithEmailAndPassword(
                    correoUsuario,
                    claveUsuario
                ).addOnCompleteListener(this) { tarea ->

                    if (tarea.isSuccessful) {

                        guardarSesion(correoUsuario)
                        abrirMenu(correoUsuario)

                    } else {

                        estado.text =
                            "Correo o contraseña incorrectos"

                    }
                }
            }
        }
    }

    private fun guardarSesion(correo: String) {

        val sesion = getSharedPreferences(
            "SesionFitBlood",
            MODE_PRIVATE
        )

        sesion.edit()
            .putString("correo", correo)
            .apply()
    }

    private fun abrirMenu(correo: String) {

        val intent = Intent(
            this,
            MenuActivity::class.java
        )

        intent.putExtra("correo", correo)

        startActivity(intent)
        finish()
    }
}











