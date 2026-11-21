gpackage com.example.proyecto_upb

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

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

            FirebaseAuth.getInstance().signOut()

            val sesion = getSharedPreferences(
                "SesionFitBlood",
                MODE_PRIVATE
            )

            sesion.edit()
                .remove("correo")
                .apply()

            val intent = Intent(this, MainActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        val textoCorreo = findViewById<TextView>(R.id.tvCorreoUsuario)
        val textoIMC = findViewById<TextView>(R.id.tvIMC)
        val textoCalorias = findViewById<TextView>(R.id.tvCalorias)
        val textoTotal = findViewById<TextView>(R.id.tvTotalCalculos)

        val usuario = FirebaseAuth.getInstance().currentUser

        textoIMC.text = "--"
        textoCalorias.text = "--"
        textoTotal.text = "Cálculos realizados: 0"

        if (usuario == null) {
            textoCorreo.text = "Usuario no identificado"
            return
        }

        textoCorreo.text = usuario.email ?: ""

        val baseDatos = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(usuario.uid)

        // Obtener el nombre del usuario
        baseDatos.child("nombre")
            .get()
            .addOnSuccessListener { dato ->

                val nombre = dato.getValue(String::class.java)

                if (!nombre.isNullOrEmpty()) {
                    textoCorreo.text = "Bienvenido, " + nombre
                } else {
                    textoCorreo.text = usuario.email ?: ""
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "No se pudo cargar el nombre",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // Obtener los cálculos guardados
        baseDatos.child("calculos")
            .get()
            .addOnSuccessListener { datos ->

                var total = 0
                var ultimaFecha = Long.MIN_VALUE
                var ultimoIMC = 0.0
                var ultimasCalorias = 0

                for (calculo in datos.children) {

                    val imc = calculo.child("imc")
                        .getValue(Double::class.java)

                    val calorias = calculo.child("calorias")
                        .getValue(Int::class.java)

                    val fecha = calculo.child("fechaMillis")
                        .getValue(Long::class.java)

                    if (imc != null &&
                        calorias != null &&
                        fecha != null) {

                        total++

                        if (fecha > ultimaFecha) {

                            ultimaFecha = fecha
                            ultimoIMC = imc
                            ultimasCalorias = calorias
                        }
                    }
                }

                textoTotal.text = "Cálculos realizados: " + total

                if (total > 0) {

                    textoIMC.text = String.format(
                        Locale.US,
                        "%.2f",
                        ultimoIMC
                    )

                    textoCalorias.text = ultimasCalorias.toString()

                } else {

                    textoIMC.text = "--"
                    textoCalorias.text = "--"
                }
            }
            .addOnFailureListener { error ->

                Toast.makeText(
                    this,
                    "Error al cargar cálculos: " +
                            error.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}









