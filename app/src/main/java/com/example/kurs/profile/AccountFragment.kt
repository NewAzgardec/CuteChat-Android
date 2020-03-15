package com.example.kurs.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.example.kurs.R
import com.example.kurs.common.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_account.*
import timber.log.Timber

class AccountFragment: Fragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser!!
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)

        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user2 = p0.getValue(User::class.java)
                if (user2 != null) {
                    name.text = user2.username
                    pbUsername.visibility = View.GONE
                }
            }
        })

        logOut.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0){
            logOut->{
                context?.let {
                    MaterialDialog(it).show {
                        message(R.string.q_exit)
                        positiveButton(R.string.yes){
                            cancel()
                            val prefs = context.getSharedPreferences(Constants.PREF, Context.MODE_PRIVATE)!!
                            val ed = prefs.edit()
                            ed?.putBoolean(Constants.IS_LOGGED, false)
                            ed?.apply()
                            FirebaseAuth.getInstance().signOut()
//                            finish()
                        }
                        negativeButton (R.string.cancel){
                            cancel()
                        }
                    }
                }
            }
        }
    }
}