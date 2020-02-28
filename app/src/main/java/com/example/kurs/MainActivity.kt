package com.example.kurs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), View.OnClickListener {

    var firebaseAuth: FirebaseAuth? = null
    var authListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()

        authListener = FirebaseAuth.AuthStateListener {auth ->
            val user = auth.currentUser
            if(user!=null){
                Timber.d("log in: "+user.uid)
            }else{
                Timber.d("log out")
            }
        }

        btnEnter.setOnClickListener(this)
        btnReg.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
       when(p0){
           btnEnter->{
                enter(etEmail.text.toString(), etPassword.text.toString())
           }

           btnReg->{
               reg(etEmail.text.toString(), etPassword.text.toString())
           }
       }
    }

    private fun enter(email: String, password: String) {
        firebaseAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener {task->
            if (task.isSuccessful){
                Toast.makeText(this, "SUCCESS LOGIN", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "ERROR LOGIN", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun reg(email: String, password: String) {
        firebaseAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener {task->
            if (task.isSuccessful){
                Toast.makeText(this, "GOOD REGISTRATION", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "ERROR REGISTRATION", Toast.LENGTH_LONG).show()
            }
        }
    }
}
