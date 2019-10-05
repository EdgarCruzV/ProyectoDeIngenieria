package com.example.beaconsfirebase


import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_register.*
import java.io.IOException
import java.util.*
import java.util.regex.Pattern


class Register : AppCompatActivity(), View.OnClickListener {
    override fun onClick(p0: View?) {
        if(p0 == picture_register){
            showFileChooser()
        }
    }
    private var PICK_IMAGE_REQUEST = 1234
    private var filePath : Uri? = null
    internal var storage : FirebaseStorage? = null
    internal var storageReference : StorageReference? = null
    private lateinit var dbReference: DatabaseReference
    private lateinit var database : FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        dbReference = database.reference.child("Users")
        to_login_activity_register.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }
        register_button_register.setOnClickListener {
            signUpUser()
        }
        picture_register.setOnClickListener(this)
    }
    private fun showFileChooser(){
        val intent = Intent()
        intent.type= "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "SELECT PICTURE"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null){
            filePath= data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                picture_register!!.setImageBitmap(bitmap)
                picture_register!!.setBackgroundResource(R.drawable.rounded_select_photo_button)
            }catch(e : IOException){
                e.printStackTrace()
            }
        }
    }
    private fun uploadPicture(uid : String){
        if(filePath != null){
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()
            val imageRef = storageReference!!.child("UserProfilePicture/" + uid)
            imageRef.putFile(filePath!!).addOnSuccessListener {
                progressDialog.dismiss()
                //Se subió la foto
            }.addOnFailureListener{
                progressDialog.dismiss()
                //No se subió la foto
            }.addOnProgressListener {taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount
                progressDialog.setMessage("Uploaded " + progress.toInt() + "%...")

            }
        }
    }
    private  fun signUpUser(){
        if(name_register.text.toString().isEmpty()){
            name_register.error = "Por favor ingresa tu nombre"
            name_register.requestFocus()
            return
        }
        if(email_register.text.toString().isEmpty()){
            email_register.error = "Por favor ingresa un email"
            email_register.requestFocus()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email_register.text.toString()).matches()){
            email_register.error = "Por favor ingresa un email válido"
            email_register.requestFocus()
            return
        }
        var itesmTecRegex : String  =
                "^[a-zA-Z0-9_.+-]+@(itesm.mx|tec.mx)$"
        var pattern : Pattern = Pattern.compile(itesmTecRegex)
        if(!pattern.matcher(email_register.text.toString()).matches()){
            email_register.error = "Por favor ingresa un email @itesm.mx o @tec.mx"
            email_register.requestFocus()
            return
        }
        if(password_register.text.toString().isEmpty()){
            password_register.error = "Por favor ingresa la contraseña"
            password_register.requestFocus()
            return
        }
        auth.createUserWithEmailAndPassword(email_register.text.toString(), password_register.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        var user = auth.currentUser
                        user?.sendEmailVerification()
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        //Sí se envió el correo
                                        val userDB = dbReference.child(user?.uid)
                                        userDB.child("Name").setValue(name_register.text.toString())
                                        uploadPicture(user?.uid.toString())
                                        Toast.makeText(
                                                baseContext,
                                                "Cuenta creada. Verifica la cuenta desde tu correo para poder ingresar.", Toast.LENGTH_SHORT
                                        ).show()


                                        startActivity(Intent(this,  Login::class.java))
                                        finish()
                                    }
                                }

                    } else {
                        var collision : FirebaseAuthUserCollisionException = task.exception as FirebaseAuthUserCollisionException

                        if(collision.errorCode == "ERROR_EMAIL_ALREADY_IN_USE") {
                            Toast.makeText(
                                    baseContext,
                                    "Ya existe la cuenta con este correo. Haz login!", Toast.LENGTH_SHORT
                            ).show();
                        }else {
                            Toast.makeText(
                                    baseContext,
                                    "Falló el registro. Confirma tus datos e inténtalo de nuevo.",
                                    Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                }
    }
}
