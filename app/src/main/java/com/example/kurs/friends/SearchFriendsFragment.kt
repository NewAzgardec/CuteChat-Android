package com.example.kurs.friends

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kurs.R
import com.example.kurs.common.Constants
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.valdesekamdem.library.mdtoast.MDToast
import kotlinx.android.synthetic.main.fragment_friends.*
import kotlinx.android.synthetic.main.fragment_search_friends.*
import timber.log.Timber
import java.lang.Exception

class SearchFriendsFragment : Fragment() {

    val list = ArrayList<User>()
    val searchList = ArrayList<User>()
    val userFriendsIds = ArrayList<String>()
    private var adapter: FriendsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler(requireContext())
        val user = FirebaseAuth.getInstance().currentUser!!
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        val currentReference = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)

        adapter = FriendsAdapter(context!!, list, true) { us, pos ->
            val referenceCurrent = FirebaseDatabase.getInstance().getReference("Users").child(us.id)
            val ref = referenceCurrent.child("newFriends").push()
            val hashMap = HashMap<String, String>()
            hashMap["id"] = user.uid
            ref.setValue(hashMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    MDToast.makeText(
                        context,
                        resources.getString(R.string.success),
                        Toast.LENGTH_LONG,
                        MDToast.TYPE_INFO
                    ).show()
                }
            }
        }

        currentReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val us = p0.getValue(User::class.java)
                if (us != null) {
                    us.friends?.forEach {
                        userFriendsIds.add(
                            it.value.toString().replace(
                                "}",
                                ""
                            ).split("=")[1]
                        )
                    }
                }

                getNewFriends(reference, user)
            }
        })


        searchNewFriend.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Timber.d(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Timber.d(s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != "") {
                    searchList.clear()
                    searchFriends(s.toString())
                } else {
                    adapter = FriendsAdapter(context!!, list, true) { us, pos ->
                        val referenceCurrent =
                            FirebaseDatabase.getInstance().getReference("Users").child(us.id)
                        val ref = referenceCurrent.child("newFriends").push()
                        val hashMap = HashMap<String, String>()
                        hashMap["id"] = user.uid
                        ref.setValue(hashMap).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                MDToast.makeText(
                                    context,
                                    resources.getString(R.string.success),
                                    Toast.LENGTH_LONG,
                                    MDToast.TYPE_INFO
                                ).show()
                            }
                        }
                    }
                    try {
                        rvSearchedFriends.adapter = adapter
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        })
    }

    private fun searchFriends(s: String) {
        list.forEach {
            if (it.lowerName.startsWith(s) && !searchList.contains(it)) {
                searchList.add(it)
                adapter = FriendsAdapter(context!!, searchList, true) { us, pos ->
                    val referenceCurrent =
                        FirebaseDatabase.getInstance().getReference("Users").child(us.id)
                    val ref = referenceCurrent.child("newFriends").push()
                    val hashMap = HashMap<String, String>()
                    hashMap["id"] = FirebaseAuth.getInstance().currentUser!!.uid
                    ref.setValue(hashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            MDToast.makeText(
                                context,
                                resources.getString(R.string.success),
                                Toast.LENGTH_LONG,
                                MDToast.TYPE_INFO
                            ).show()
                        }
                    }
                }
                try {
                    rvSearchedFriends.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
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
                    if (user2 != null && user2.id != user.uid && !userFriendsIds.contains(user2.id)) {
                        list.add(user2)
                    }
                }
                try {
                    rvSearchedFriends.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }


    private fun setupRecycler(context: Context) {
        with(rvSearchedFriends) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
    }
}