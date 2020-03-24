package com.example.kurs

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.kurs.friends.FriendsFragment
import com.example.kurs.messages.ChatsFragment
import com.example.kurs.messages.message.Message
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
import com.rom4ek.arcnavigationview.ArcNavigationView
import kotlinx.android.synthetic.main.enter_activity.*
import kotlinx.android.synthetic.main.header_layout.*
import timber.log.Timber

class EnterActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    val user = FirebaseAuth.getInstance().currentUser!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.enter_activity)
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, AccountFragment())
            .addToBackStack(null).commit()
        val dr = findViewById<DrawerLayout>(R.id.drawer_layout)
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


        val toggle = ActionBarDrawerToggle(
            this, dr, null,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        dr.addDrawerListener(toggle)
        toggle.syncState()
        messagesCount()
        arcNavigationView.setNavigationItemSelectedListener(this)
    }

    private fun messagesCount() {
        val nav = findViewById<ArcNavigationView>(R.id.arcNavigationView)
        val item = nav.menu.findItem(R.id.action_messages)

        val reference = FirebaseDatabase.getInstance().getReference("Messages")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                var unreadCount = 0
                p0.children.forEach {
                    val chat = it.getValue(Message::class.java)
                    if (chat != null) {
                        if (chat.receiverId ==user.uid&&!chat.seen){
                            unreadCount++
                        }
                    }
                }
                if(unreadCount == 0){
                    item.title = "Сообщения"
                }else{
                    item.title = "Сообщения ($unreadCount)"
                }
            }
        })
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        var fragment: Fragment? = null

        when (p0.itemId) {
            R.id.action_profile -> {
                fragment = AccountFragment()
            }
            R.id.action_friends -> {
                fragment = FriendsFragment()
            }
            R.id.action_messages -> {
                fragment = ChatsFragment()
            }
            R.id.action_wall -> {
                fragment = WallFragment()
            }
            R.id.action_settings -> {
                fragment = SettingsFragment()
            }
        }
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment)
                .addToBackStack(null).commit()
        }
        findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.END)
        return true
    }

    override fun onBackPressed() {
        if (findViewById<DrawerLayout>(R.id.drawer_layout).isDrawerOpen(GravityCompat.END)) {
            findViewById<DrawerLayout>(R.id.drawer_layout).closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        setStatus(false)
    }

    private fun setStatus(isOnline: Boolean) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val reference = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)
            val hashMap = HashMap<String, Any>()
            if (isOnline) {
                hashMap["onlineStatus"] = "true"
            } else {
                hashMap["onlineStatus"] = "false"
            }
            reference.updateChildren(hashMap)
        }
    }

    override fun onResume() {
        super.onResume()
        setStatus(true)
    }
}