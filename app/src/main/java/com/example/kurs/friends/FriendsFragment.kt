package com.example.kurs.friends

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kurs.R
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_friends.*
import timber.log.Timber

class FriendsFragment : Fragment(), View.OnClickListener {

    val list = ArrayList<User>()
    val newUsers = ArrayList<User>()
    private var adapter: FriendsAdapter? = null
    private var newUsersAdapter: NewFriendsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler(requireContext())
        val user = FirebaseAuth.getInstance().currentUser!!


        adapter = FriendsAdapter(context!!, list, true) { us, pos ->

        }
        searchFriend.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Timber.d(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Timber.d(s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchFriends(s.toString())
            }

        })

        checkNewFriends(user)
        checkFriends(user)

        btnAddFriend.setOnClickListener(this)
        btnNewFriends.setOnClickListener(this)
        cbOnline.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val newList = ArrayList<User>()
                list.forEach {
                    if (it.onlineStatus == "true") {
                        newList.add(it)
                    }
                }
                adapter = FriendsAdapter(context!!, newList, true) { us, pos ->

                }
                try {
                    rvFriends.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                adapter = FriendsAdapter(context!!, list, true) { us, pos ->

                }
                try {
                    rvFriends.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
    }

    private fun searchFriends(s: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("lowerName")
            .startAt(s).endAt(s + "\uf8ff")
        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                list.clear()
                p0.children.forEach {
                    val u = it.getValue(User::class.java)
                    if (u != null && user != null) {
                        if (u.id != user.uid) {
                            list.add(u)
                        }
                    }
                }

                try {
                    rvFriends.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        })
    }

    private fun checkFriends(user: FirebaseUser) {
        val reference =
            FirebaseDatabase.getInstance().getReference("Users").child(user.uid).child("friends")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val value = it.value.toString().replace("{", "").replace("}", "").split("=")[1]
                    val ref = FirebaseDatabase.getInstance().getReference("Users")
                        .child(value)
                    list.clear()

                    ref.addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Timber.d(p0.message)
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val friend = p0.getValue(User::class.java)

                            if (friend != null) {
                                !checkList(friend)
                                list.add(friend)
                            }

                            try {
                                rvFriends.adapter = adapter
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                }
            }
        })
    }

    private fun checkList(friend: User): Boolean {
        val user = User(
            friend.id,
            friend.username,
            friend.lowerName,
            friend.email,
            friend.password,
            friend.imageUri,
            friend.friends,
            ""
        )
        list.forEach {
            val user2 =
                User(it.id, it.username, it.lowerName, it.email, it.password, it.imageUri, it.friends, "")
            if (user2 == user) {
                list.remove(it)
                return true
            }
        }
        return false
    }

    private fun checkNewFriends(user: FirebaseUser) {
        val referenceCurrent = FirebaseDatabase.getInstance().getReference("Users")
        val newFriendsReference = referenceCurrent.child(user.uid).child("newFriends")
        newUsersAdapter = NewFriendsAdapter(newUsers, { us, pos ->
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

                list.add(user1)
                adapter?.notifyDataSetChanged()

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
                            if (newFriend != null && !list.contains(newFriend)) {
                                newUsers.add(newFriend)
                            }
                        }
                    })
                }
            }
        })
    }

    private fun removeFromNewList(newFriendsReference: DatabaseReference, user1: User) {
        newFriendsReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.children.count() > 0) {
                    p0.children.forEach {
                        val value = it.value.toString().replace("}", "").split("=")[1]
                        if (value == user1.id) {
                            newFriendsReference.setValue(it.key, null)
                        }
                    }
                } else {
                    cvNewFriends.visibility = View.GONE
                }

            }
        })
    }


    private fun setupRecycler(context: Context) {
        with(rvFriends) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
        with(rvNewFriends) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btnAddFriend -> {
                val fragment = SearchFriendsFragment()
                fragmentManager?.beginTransaction()?.add(R.id.frameLayout, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
            }
            btnNewFriends -> {
                if (newUsers.isNotEmpty()) {
                    cvNewFriends.visibility = View.VISIBLE
                    rvNewFriends.adapter = newUsersAdapter
                }
            }
        }
    }
}