package com.example.beaconsfirebase.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.beaconsfirebase.R


import android.content.Intent
import android.widget.Toast
import com.example.beaconsfirebase.Login
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_manage_account.*





class Settings : Fragment() {
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        auth = FirebaseAuth.getInstance()

        return inflater.inflate(R.layout.fragment_tools, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                                    Toast.makeText(context, "Verificación de cuenta exitosa.",
                                            Toast.LENGTH_SHORT).show()

                                    user?.updatePassword(new_pasword_manage_account.text.toString())
                                            ?.addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Toast.makeText(context, "Se actualizó la nueva contraseña.",
                                                            Toast.LENGTH_SHORT).show()
                                                    auth.signOut()
                                                    val intent = Intent(activity, Login::class.java)
                                                    startActivity(intent)
                                                }else{
                                                    Toast.makeText(context, "No se pudo actualizar la contraseña.",
                                                            Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                }else{
                                    Toast.makeText(context, "Verificación de cuenta fallida .",
                                            Toast.LENGTH_SHORT).show()
                                }
                            }
                }else{
                    val intent = Intent(activity, Login::class.java)
                    startActivity(intent)
                }
            }else{
                Toast.makeText(context, "La nueva contraseña no coincide.",
                        Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context, "Por favor ingresa todos los campos.",
                    Toast.LENGTH_SHORT).show()
        }
    }
}
