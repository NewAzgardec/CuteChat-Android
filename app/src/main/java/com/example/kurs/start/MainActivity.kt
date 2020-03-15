package com.example.kurs.start

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.kurs.EnterActivity
import com.example.kurs.R
import com.example.kurs.common.Constants
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = this.getSharedPreferences(Constants.PREF, Context.MODE_PRIVATE)!!
        if(prefs.getBoolean(Constants.IS_LOGGED, false)){
            finish()
            startActivity(Intent(this, EnterActivity::class.java))
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
