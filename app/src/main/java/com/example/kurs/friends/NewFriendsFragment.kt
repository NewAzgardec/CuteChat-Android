package com.example.kurs.friends

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kurs.R
import com.example.kurs.friends.account.FriendAccount
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_search_friends.*
import timber.log.Timber

class NewFriendsFragment : Fragment() {

    val newUsers = ArrayList<User>()
    private var newUsersAdapter: NewFriendsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTitle.text = resources.getString(R.string.requests)
        setupRecycler(requireContext())

        checkNewFriends(FirebaseAuth.getInstance().currentUser!!)
    }

    private fun checkNewFriends(user: FirebaseUser) {
        val referenceCurrent = FirebaseDatabase.getInstance().getReference("Users")
        val newFriendsReference = referenceCurrent.child(user.uid).child("newFriends")
        newUsersAdapter = NewFriendsAdapter(context!!, newUsers, { us, pos ->
            openFriendProfile(us)
        },
            { user1, i ->
                removeFromNewList(newFriendsReference, user1)
                val ref = referenceCurrent.child(user.uid).child("friends").push()
                val hashMap = HashMap<String, String>()
                hashMap["id"] = user1.id
                ref.setValue(hashMap)

                val ref2 = referenceCurrent.child(user1.id).child("friends").push()
                val hashMap2 = HashMap<String, String>()
                hashMap2["id"] = user.uid
                ref2.setValue(hashMap2)

            }, { user2, i ->
                removeFromNewList(newFriendsReference, user2)
            })


        newFriendsReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                newUsers.clear()
                p0.children.forEach {
                    val value = it.value.toString().replace("}", "").split("=")[1]
                    val ref = FirebaseDatabase.getInstance().getReference("Users")
                        .child(value)
                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Timber.d(p0.message)
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val newFriend = p0.getValue(User::class.java)
                            if (newFriend != null) {
                                newUsers.add(newFriend)
                            }
                            deleteVisibility(newUsers)
                            try {
                                rvSearchedFriends.adapter = newUsersAdapter
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                }
                if(p0.children.count()==0) deleteVisibility(newUsers)
            }
        })

        btnSearchBack.setOnClickListener { activity!!.onBackPressed() }
    }

    private fun deleteVisibility(newUsers: ArrayList<User>) {
        try {
            if (newUsers.isEmpty()) {
                noUsers.visibility = View.VISIBLE
                searchNewFriend.visibility = View.GONE
            } else {
                noUsers.visibility = View.GONE
                searchNewFriend.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openFriendProfile(us: User) {
        val fragment = FriendAccount()
        val args = Bundle()
        args.putString("friendId", us.id)
        args.putBoolean("isExists", false)
        fragment.arguments = args
        fragmentManager?.beginTransaction()?.add(R.id.frameLayout, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }


    private fun removeFromNewList(newFriendsReference: DatabaseReference, user1: User) {
        newFriendsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.children.count() > 0) {
                    p0.children.forEach {
                        val value = it.value.toString().replace("}", "").split("=")[1]
                        if (value == user1.id) {
                            newFriendsReference.child(it.key.toString()).setValue(null)
                        }
                    }
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