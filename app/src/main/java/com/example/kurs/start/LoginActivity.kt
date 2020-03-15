package com.example.kurs.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kurs.EnterActivity
import com.example.kurs.R
import com.example.kurs.common.Constants
import com.google.firebase.auth.FirebaseAuth
import com.valdesekamdem.library.mdtoast.MDToast
import kotlinx.android.synthetic.main.activity_main.btnEnter
import kotlinx.android.synthetic.main.login_activity.*

class LoginActivity: AppCompatActivity(), View.OnClickListener {

    private var firebaseAuth: FirebaseAuth? = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        btnEnter.setOnClickListener(this)
        btnForgetPassword.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0){
            btnEnter->{
                if(etEmail.text.trim().isNotEmpty()&&etPassword.text.trim().isNotEmpty()) {
                    enter(etEmail.text.toString(), etPassword.text.toString())
                }else{
                    MDToast.makeText(this, resources.getString(R.string.empty), Toast.LENGTH_LONG, MDToast.TYPE_WARNING).show()
                }
            }
            btnForgetPassword->{
                if(etEmail.text.trim().isNotEmpty()) {
                    firebaseAuth?.sendPasswordResetEmail(etEmail.text.toString())
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                MDToast.makeText(
                                    this,
                                    resources.getString(R.string.sended_msg),
                                    Toast.LENGTH_LONG,
                                    MDToast.TYPE_INFO
                                ).show()
                            } else {
                                MDToast.makeText(
                                    this,
                                    task.exception?.localizedMessage,
                                    Toast.LENGTH_LONG,
                                    MDToast.TYPE_ERROR
                                ).show()
                            }
                        }
                }else{
                    MDToast.makeText(this, resources.getString(R.string.empty), Toast.LENGTH_LONG, MDToast.TYPE_WARNING).show()
                }
            }
        }
    }

    private fun enter(email: String, password: String) {
        firebaseAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener {task->
            if (task.isSuccessful){
                val user = FirebaseAuth.getInstance().currentUser
                if(user!=null&&user.isEmailVerified){
                    val prefs = this.getSharedPreferences(Constants.PREF, Context.MODE_PRIVATE)!!
                    val ed = prefs.edit()
                    ed?.putBoolean(Constants.IS_LOGGED, true)
                    ed?.apply()
                    MDToast.makeText(this, resources.getString(R.string.success), Toast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show()
                    finishAffinity()
                    startActivity(Intent(this, EnterActivity::class.java))
                }else{
                    MDToast.makeText(this, resources.getString(R.string.confirm_email), Toast.LENGTH_LONG, MDToast.TYPE_INFO).show()
                }
            }else{
                MDToast.makeText(this, task.exception?.localizedMessage, Toast.LENGTH_LONG, MDToast.TYPE_ERROR).show()
            }
        }
    }
}