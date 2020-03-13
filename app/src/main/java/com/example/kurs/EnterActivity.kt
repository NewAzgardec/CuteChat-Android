package com.example.kurs

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.enter_activity.*

class EnterActivity :AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.enter_activity)

        val dr = findViewById<DrawerLayout>(R.id.drawer_layout)
        val tb = findViewById<Toolbar>(R.id.toolbar)


        setSupportActionBar(tb)
        val toggle = ActionBarDrawerToggle(this, dr, tb,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        dr.addDrawerListener(toggle)
        toggle.syncState()

        arcNavigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}