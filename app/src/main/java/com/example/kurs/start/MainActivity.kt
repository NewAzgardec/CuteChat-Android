package com.example.kurs.start

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.kurs.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var authListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
               startActivity(Intent(this, LoginActivity::class.java))
           }

           btnReg->{
               startActivity(Intent(this, RegistrationActivity::class.java))
           }
       }
    }
}
