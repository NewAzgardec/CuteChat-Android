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
import com.google.firebase.database.FirebaseDatabase
import com.valdesekamdem.library.mdtoast.MDToast
import kotlinx.android.synthetic.main.activity_main.btnReg
import kotlinx.android.synthetic.main.registration_activity.*
import java.security.MessageDigest

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
                if(etEmail.text.trim().isNotEmpty()&&etPassword.text.trim().isNotEmpty()&&userName.text.trim().isNotEmpty()) {
                    reg(
                        etEmail.text.toString(),
                        etPassword.text.toString(),
                        userName.text.toString()
                    )
                }else{
                    MDToast.makeText(this, resources.getString(R.string.empty), Toast.LENGTH_LONG, MDToast.TYPE_WARNING).show()
                }
            }
        }
    }

    private fun sha256(base: String): String {
        return try {
            val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
            val hash: ByteArray = digest.digest(base.toByteArray(charset("UTF-8")))
            val hexString = StringBuffer()
            for (i in hash.indices) {
                val hex = Integer.toHexString(0xff and hash[i].toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            hexString.toString()
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
    }

    private fun reg(email: String, password: String, username: String) {
        firebaseAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth!!.currentUser
                    if (user != null) {
                        MDToast.makeText(this, resources.getString(R.string.verification), Toast.LENGTH_LONG, MDToast.TYPE_INFO).show()
                        user.sendEmailVerification().addOnCompleteListener {task1->
                            if(task1.isSuccessful){
                                val reference =
                                    FirebaseDatabase.getInstance().getReference(Constants.USERS)
                                        .child(user.uid)
                                val hashMap = HashMap<String, String>()
                                hashMap[Constants.ID] = user.uid
                                hashMap[Constants.USERNAME] = username
                                hashMap["lowerName"] = username.toLowerCase()
                                hashMap["onlineStatus"] = "online"
                                hashMap[Constants.EMAIL] = email
                                hashMap[Constants.PASSWORD] = sha256(password)
                                reference.setValue(hashMap).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val prefs = this.getSharedPreferences(Constants.PREF, Context.MODE_PRIVATE)!!
                                        val ed = prefs.edit()
                                        ed?.putBoolean(Constants.IS_LOGGED, true)
                                        ed?.apply()
                                        finishAffinity()
                                        startActivity(
                                            Intent(
                                                this,
                                                EnterActivity::class.java
                                            )
                                        )
                                    }
                                }
                                MDToast.makeText(this,resources.getString(R.string.success), Toast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show()
                            }else{
                                MDToast.makeText(this,task.exception?.localizedMessage, Toast.LENGTH_LONG, MDToast.TYPE_ERROR).show()
                            }
                        }
                    } else {
                        MDToast.makeText(this,resources.getString(R.string.error), Toast.LENGTH_LONG, MDToast.TYPE_ERROR).show()
                    }
                }else{
                    MDToast.makeText(this,task.exception?.localizedMessage, Toast.LENGTH_LONG, MDToast.TYPE_ERROR).show()
                }
            }
    }
}