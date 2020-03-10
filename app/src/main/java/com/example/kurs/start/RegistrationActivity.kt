package com.example.kurs.start

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kurs.Account
import com.example.kurs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.btnReg
import kotlinx.android.synthetic.main.registration_activity.*

class RegistrationActivity : AppCompatActivity(), View.OnClickListener {

    private var firebaseAuth: FirebaseAuth? = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_activity)
        btnReg.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {

            btnReg -> {
                reg(etEmail.text.toString(), etPassword.text.toString(), userName.text.toString())
            }

        }
    }

    private fun reg(email: String, password: String, username: String) {
        firebaseAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = firebaseAuth!!.currentUser
                    if (user != null) {

                        val reference =
                            FirebaseDatabase.getInstance().getReference("Users").child(user.uid)
                        val hashMap = HashMap<String, String>()
                        hashMap["id"] = user.uid
                        hashMap["username"] = username
                        hashMap["email"] = email
                        hashMap["password"] = password
                        reference.setValue(hashMap).addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                finish()
                                startActivity(Intent(this, Account::class.java))
                            }
                        }
                        Toast.makeText(this, "GOOD REGISTRATION", Toast.LENGTH_LONG).show()
                    }


                } else {
                    Toast.makeText(this, "ERROR REGISTRATION", Toast.LENGTH_LONG).show()
                }
            }
    }
}