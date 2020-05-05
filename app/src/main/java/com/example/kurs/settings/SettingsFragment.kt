package com.example.kurs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.kurs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser!!
        val referenceSettings = FirebaseDatabase.getInstance().getReference("Settings")

        val darkTheme = activity!!.findViewById<SwitchCompat>(R.id.switchDarkTheme)
        val engLang = activity!!.findViewById<SwitchCompat>(R.id.switchEnglishLanguage)

        val ref = FirebaseDatabase.getInstance().getReference("Settings")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                var isExists = false
                p0.children.forEach {
                    val set = it.getValue(Setting::class.java)
                    if (set != null && set.userId == user.uid) {
                        isExists = true
                    }
                }

                if (!isExists) {
                    val hashMap = HashMap<String, Any>()
                    hashMap["darkTheme"] = darkTheme.isChecked.toString()
                    hashMap["userId"] = FirebaseAuth.getInstance().currentUser!!.uid
                    hashMap["engLang"] = engLang.isChecked.toString()
                    ref.push().setValue(hashMap)
                }
            }
        })


        darkTheme.setOnCheckedChangeListener { _, isChecked ->
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Timber.d(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        val s = it.getValue(Setting::class.java)
                        if (s != null && s.userId == user.uid) {
                            val hashMap = HashMap<String, Any>()
                            hashMap["darkTheme"] = isChecked.toString()
                            ref.child(it.key.toString()).updateChildren(hashMap)
                        }
                    }
                }
            })
        }

        engLang.setOnCheckedChangeListener { _, isChecked ->
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Timber.d(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    p0.children.forEach {
                        val s = it.getValue(Setting::class.java)
                        if (s != null && s.userId == user.uid) {
                            val hashMap = HashMap<String, Any>()
                            hashMap["engLang"] = isChecked.toString()
                            ref.child(it.key.toString()).updateChildren(hashMap)
                        }
                    }
                }
            })
        }

        referenceSettings.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val set = it.getValue(Setting::class.java)
                    if (set != null && set.userId == user.uid) {
                        darkTheme.isChecked = set.darkTheme == "true"
                        engLang.isChecked = set.engLang == "true"
                    }
                }
            }
        })
    }
}