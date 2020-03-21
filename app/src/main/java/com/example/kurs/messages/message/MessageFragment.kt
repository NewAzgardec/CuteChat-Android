package com.example.kurs.messages.message

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kurs.R
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_messages.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MessageFragment : Fragment(), View.OnClickListener {
    private var adapter: MessageAdapter? = null
    var messages = ArrayList<Message>()
    var receiver = ""
    var userId = ""
    var userName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler(requireContext())

        if (arguments != null) {
            receiver = arguments!!.getString("receiver", "")
        }

        userId = FirebaseAuth.getInstance().currentUser!!.uid

        adapter = MessageAdapter(messages){m, i->}


        val currentReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(receiver)
        currentReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val u = p0.getValue(User::class.java)
                if (u != null) {
                    userName = u.username
                }
            }

        })


        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                readMessages(FirebaseAuth.getInstance().currentUser!!.uid, receiver)
            }

        })

        btnSend.setOnClickListener(this)
    }

    private fun setupRecycler(context: Context) {
        with(rvMessages) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
    }

    private fun readMessages(uid: String, receiver: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Messages")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                messages.clear()
                p0.children.forEach {
                    val message = it.getValue(Message::class.java)

                    if (message != null) {
                        if (message.receiverId == uid && message.senderId == receiver || message.receiverId == receiver && message.senderId == uid) {
                            messages.add(message)
                        }
                    }
                }

                rvMessages.adapter = adapter
            }

        })

    }

    override fun onClick(v: View?) {
        when (v) {
            btnSend -> {
                val reference = FirebaseDatabase.getInstance().getReference("Messages")
                val hashMap = HashMap<String, String>()
                hashMap["senderId"] = userId
                hashMap["senderName"] = userName
                hashMap["receiverId"] = receiver
                hashMap["text"] = etMessage.text.trim().toString()
                hashMap["date"] = Date().toString()

                reference.push().setValue(hashMap)
                etMessage.clearFocus()
                etMessage.setText("")
            }
        }
    }
}