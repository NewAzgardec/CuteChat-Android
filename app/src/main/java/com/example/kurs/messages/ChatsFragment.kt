package com.example.kurs.messages

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kurs.R
import com.example.kurs.friends.FriendsAdapter
import com.example.kurs.messages.message.Message
import com.example.kurs.messages.message.MessageFragment
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_chats.*
import timber.log.Timber

class ChatsFragment : Fragment(), View.OnClickListener {
    private var adapter: FriendsAdapter? = null
    val list = ArrayList<String>()
    val chats = ArrayList<User>()
    var userId = ""
    var sPref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler(requireContext())
        sPref = context?.getSharedPreferences("pref", MODE_PRIVATE)
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = FirebaseDatabase.getInstance().getReference("Messages")

        adapter = FriendsAdapter(context!!, chats, false) { us, pos ->
            val bundle = Bundle()
            bundle.putString("receiver", us.id)
            val fragment = MessageFragment()
            fragment.arguments = bundle
            fragmentManager!!.beginTransaction().add(R.id.frameLayout, fragment)
                .addToBackStack(null).commit()
        }

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                list.clear()
                p0.children.forEach {
                    val chat = it.getValue(Message::class.java)
                    if(chat != null &&!list.contains(chat.senderId)&&!list.contains(chat.receiverId)) {
                        if (chat.senderId == userId) {
                            list.add(chat.receiverId)
                        }
                        if (chat.receiverId == userId) {
                            list.add(chat.senderId)
                        }
                    }
                }
                val ed = sPref?.edit()
                val set = HashSet<String>()
                set.addAll(list)
                if (ed != null) {
                    ed.putStringSet("chats", set)
                    ed.apply()
                }
                try {
                    getChats()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        btnAddChat.setOnClickListener(this)
    }

    private fun getChats() {
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                chats.clear()
                p0.children.forEach {
                    val user = it.getValue(User::class.java)
                    for(i in 0 until list.size) {
                        if (user != null) {
                            if (user.id == list[i]) {
                                if (chats.size != 0) {
                                    for(n in 0 until chats.size) {
                                        if (user.id != chats[n].id) {
                                            chats.add(user)
                                        }
                                    }
                                } else {
                                    chats.add(user)
                                }
                            }

                        }
                    }

                }
                try {
                    rvChats.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        })
    }

    private fun setupRecycler(context: Context) {
        with(rvChats) {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            setHasFixedSize(false)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btnAddChat -> {
                val fragment = SelectReceiverFragment()
                fragmentManager?.beginTransaction()?.add(R.id.frameLayout, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
            }
        }
    }
}