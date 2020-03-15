package com.example.kurs

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.kurs.messages.MessagesFragment
import com.example.kurs.profile.AccountFragment
import com.example.kurs.profile.User
import com.example.kurs.settings.SettingsFragment
import com.example.kurs.wall.WallFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.enter_activity.*
import kotlinx.android.synthetic.main.header_layout.*
import timber.log.Timber

class EnterActivity :AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.enter_activity)

        val dr = findViewById<DrawerLayout>(R.id.drawer_layout)
        val tb = findViewById<Toolbar>(R.id.toolbar)
        val user = FirebaseAuth.getInstance().currentUser!!
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user2 = p0.getValue(User::class.java)
                if (user2 != null) {
                    nickname.text = user2.username
                }
            }
        })


        setSupportActionBar(tb)
        val toggle = ActionBarDrawerToggle(this, dr, tb,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        dr.addDrawerListener(toggle)
        toggle.syncState()

        arcNavigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        var fragment: Fragment? = null
        when(p0.itemId){
            R.id.action_profile->{
                fragment = AccountFragment()
            }
            R.id.action_messages->{
                fragment = MessagesFragment()
            }
            R.id.action_wall->{
                fragment = WallFragment()
            }
            R.id.action_settings->{
                fragment = SettingsFragment()
            }
        }
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()
        }
//        p0.isChecked = true
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.END)
        return true
    }

    override fun onBackPressed() {
        if(findViewById<DrawerLayout>(R.id.drawer_layout).isDrawerOpen(GravityCompat.END)){
            findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.END)
        }else {
            super.onBackPressed()
        }
    }
}