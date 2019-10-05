package com.example.beaconsfirebase


import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.regex.Pattern

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
        to_register_activity_login.setOnClickListener {
            startActivity(Intent(this,  Register::class.java ))
            finish()
        }
        login_button_login.setOnClickListener {
            val isThereAProblem = checkEmailError()
            if(!isThereAProblem){
                login()
            }
        }
        forgot_password_login.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Olvidé mi Contraseña")
            val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
            val username = view.findViewById<EditText>(R.id.username_dialog_forgot_password)
            builder.setView(view)
            builder.setPositiveButton("Resetear Contraseña", DialogInterface.OnClickListener { dialogInterface, i ->
                forgotPassword(username)
            })
            builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialogInterface, i ->  })
            builder.show()
        }
    }
    private fun forgotPassword(username : EditText){
        if(username.text.toString().isEmpty()){
            Toast.makeText(baseContext, "Correo inválido.",
                    Toast.LENGTH_SHORT).show()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()){
            Toast.makeText(baseContext, "Correo inválido.",
                    Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(username.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Se te envío un correo.",
                                Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(baseContext, "Correo inválido.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }
    private  fun checkEmailError() : Boolean{
        if(email_login.text.toString().isEmpty()){
            email_login.error = "Por favor ingresa un email"
            email_login.requestFocus()
            return true
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email_login.text.toString()).matches()){
            email_login.error = "Por favor ingresa un email válido"
            email_login.requestFocus()
            return true
        }
        var itesmTecRegex : String  =
                "^[a-zA-Z0-9_.+-]+@(itesm.mx|tec.mx)$"
        var pattern : Pattern = Pattern.compile(itesmTecRegex)
        if(!pattern.matcher(email_login.text.toString()).matches()){
            email_login.error = "Por favor ingresa un email @itesm.mx o @tec.mx"
            email_login.requestFocus()
            return true
        }
        if(password_login.text.toString().isEmpty()){
            password_login.error = "Por favor ingresa la contraseña"
            password_login.requestFocus()
            return true
        }
        return false
    }


    fun updateUI(currentUser : FirebaseUser?){
        if(currentUser != null){
            if(currentUser.isEmailVerified){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                Toast.makeText(baseContext, "Por favor verifica tu cuenta desde tu correo.",
                        Toast.LENGTH_SHORT).show()
            }

        }
    }
    fun login(){
        auth.signInWithEmailAndPassword(email_login.text.toString(), password_login.text.toString())
                .addOnCompleteListener(this) { task ->
                    var exception: FirebaseAuthInvalidUserException
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = auth.currentUser
                        updateUI(user)
                    } else {

                        // If sign in fails, display a message to the user.Log.w(TAG, "signInWithEmail:failure", task.exception)
                        try {
                            exception = task.exception as FirebaseAuthInvalidUserException
                            if (exception.errorCode == "ERROR_USER_NOT_FOUND") {
                                Toast.makeText(
                                        baseContext, "No existe la cuenta, por favor crea una.",
                                        Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this, Register::class.java))
                            }
                        } catch (e: ClassCastException) {

                            Toast.makeText(
                                    baseContext, "Algo falló. Verifica tus datos e inténtalo de nuevo.",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    updateUI(null)
                }
    }
}
