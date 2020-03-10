package com.example.kurs.start

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kurs.Account
import com.example.kurs.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.btnEnter
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity: AppCompatActivity(), View.OnClickListener {


    private var firebaseAuth: FirebaseAuth? = FirebaseAuth.getInstance()
//    var db: FirebaseDatabase? = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        btnEnter.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0){
            btnEnter->{
                enter(etEmail.text.toString(), etPassword.text.toString())
            }
        }
    }


    private fun enter(email: String, password: String) {
        firebaseAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener {task->
            if (task.isSuccessful){
                Toast.makeText(this, "SUCCESS LOGIN", Toast.LENGTH_LONG).show()
                finish()
                startActivity(Intent(this, Account::class.java))
            }else{
                Toast.makeText(this, "ERROR LOGIN", Toast.LENGTH_LONG).show()
            }
        }
    }

}