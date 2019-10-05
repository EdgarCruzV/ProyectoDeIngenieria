package com.example.beaconsfirebase


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_manage_account.*

class ManageAccount : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)
        auth = FirebaseAuth.getInstance()
        change_password_button_manage_account.setOnClickListener {
            changePassword()
        }
    }
    private fun changePassword(){
        if(current_pasword_manage_account.text.isNotEmpty() && confirm_new_pasword_manage_account.text.isNotEmpty() && new_pasword_manage_account.text.isNotEmpty() ){
            if(new_pasword_manage_account.text.toString().equals(confirm_new_pasword_manage_account.text.toString())){

                val user = FirebaseAuth.getInstance().currentUser
                if(user != null && user.email != null){
                    val credential = EmailAuthProvider
                            .getCredential(user.email!!, current_pasword_manage_account.text.toString() )
                    user?.reauthenticate(credential)
                            ?.addOnCompleteListener {
                                if(it.isSuccessful){
                                    Toast.makeText(baseContext, "Verificación de cuenta exitosa.",
                                            Toast.LENGTH_SHORT).show()

                                    user?.updatePassword(new_pasword_manage_account.text.toString())
                                            ?.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(baseContext, "Se actualizó la nueva contraseña.",
                                                            Toast.LENGTH_SHORT).show()
                                                    auth.signOut()
                                                    startActivity(Intent(this, Login::class.java ))
                                                    finish()
                                                }else{
                                                    Toast.makeText(baseContext, "No se pudo actualizar la contraseña.",
                                                            Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                }else{
                                    Toast.makeText(baseContext, "Verificación de cuenta fallida .",
                                            Toast.LENGTH_SHORT).show()
                                }
                            }
                }else{
                    startActivity(Intent(this, Login::class.java))
                }
            }else{
                Toast.makeText(baseContext, "La nueva contraseña no coincide.",
                        Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(baseContext, "Por favor ingresa todos los campos.",
                    Toast.LENGTH_SHORT).show()
        }
    }
}