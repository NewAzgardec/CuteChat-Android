package com.example.kurs.messages

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kurs.R
import com.example.kurs.friends.FriendsAdapter
import com.example.kurs.messages.message.MessageFragment
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_select_receiver.*
import timber.log.Timber


class SelectReceiverFragment : Fragment(), View.OnClickListener {
    val list = ArrayList<User>()
    val userFriendsIds = ArrayList<String>()
    val existList = ArrayList<String>()
    private var adapter: FriendsAdapter? = null
    var sPref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_receiver, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler(requireContext())

        val user = FirebaseAuth.getInstance().currentUser!!
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        val currentReference = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)

        adapter = FriendsAdapter(context!!, list, true) { us, pos ->
            val bundle = Bundle()
            bundle.putString("receiver", us.id)
            val fragment = MessageFragment()
            fragment.arguments = bundle
            fragmentManager!!.popBackStack()
            fragmentManager!!.beginTransaction().replace(R.id.frameLayout, fragment)
                .addToBackStack(null).commit()
        }
        currentReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                sPref = context?.getSharedPreferences("pref", Context.MODE_PRIVATE)
                sPref?.getStringSet("chats", null)?.let { existList.addAll(it) }

                val us = p0.getValue(User::class.java)
                if (us != null) {
                    us.friends?.forEach {
                        val id = it.value.toString().replace("}", "").split("=")[1]
                        if (!existList.contains(id)) {
                            userFriendsIds.add(id)
                        }
                    }
                }

                getNewFriends(reference, user)
            }
        })


        ivBackFromReceivers.setOnClickListener(this)

    }

    private fun getNewFriends(reference: DatabaseReference, user: FirebaseUser) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                list.clear()
                p0.children.forEach {
                    val user2 = it.getValue(User::class.java)
                    if (user2 != null && user2.id != user.uid && userFriendsIds.contains(user2.id)) {
                        list.add(user2)
                    }
                    if (list.isEmpty()) noUsers.visibility = View.VISIBLE
                    else noUsers.visibility = View.GONE
                }
                try {

                    rvReceivers.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            ivBackFromReceivers -> {
                fragmentManager?.popBackStack()
            }
        }
    }

    private fun setupRecycler(context: Context) {
        with(rvReceivers) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
    }
}