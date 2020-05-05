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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_friends.*
import timber.log.Timber

class FriendsFragment : Fragment(), View.OnClickListener {

    val list = ArrayList<User>()
    val list2 = ArrayList<User>()
    val newList = ArrayList<User>()
    private var adapter: FriendsAdapter? = null

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
                if (s != null && s.isNotEmpty()) {
                    searchFriends(s.toString())
                } else {
                    if (cbOnline.isChecked) {
                        adapter = FriendsAdapter(context!!, newList, true) { us, pos ->

                        }
                        setFriendCount(newList)
                        rvFriends.adapter = adapter
                    } else {
                        adapter = FriendsAdapter(context!!, list, true) { us, pos ->

                        }
                        setFriendCount(list)
                        rvFriends.adapter = adapter
                    }
                }
            }

        })

        checkFriends(user)

        btnAddFriend.setOnClickListener(this)
        btnNewFriends.setOnClickListener(this)
        cbOnline.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                newList.clear()
                list.forEach {
                    if (it.onlineStatus == "true") {
                        newList.add(it)
                    }
                }
                deleteVisibility()
                adapter = FriendsAdapter(context!!, newList, true) { us, pos ->

                }
                try {
                    setFriendCount(newList)
                    rvFriends.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                adapter = FriendsAdapter(context!!, list, true) { us, pos ->

                }
                try {
                    setFriendCount(list)
                    rvFriends.adapter = adapter
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setFriendCount(friends: ArrayList<User>) {
        friendsCount.text = friends.size.toString() + " " + getCount(friends.size)
    }

    private fun getCount(number: Int): String {
        var n = kotlin.math.abs(number)
        n %= 100
        if (n in 5..20) {
            return resources.getString(R.string.friends_5)
        }
        n %= 10
        if (n == 1) {
            return resources.getString(R.string.friens_1)
        }
        if (n in 2..4) {
            return resources.getString(R.string.friend_2)
        }
        return resources.getString(R.string.friends_5)
    }

    private fun deleteVisibility() {
        if (list.isEmpty()) {
            friendsCount.visibility = View.GONE
            cbOnline.visibility = View.GONE
            online.visibility = View.GONE
            online.visibility = View.GONE
//            searchFriend.isFocusable = false
            noFriends.visibility = View.VISIBLE
        } else {
            friendsCount.visibility = View.VISIBLE
            cbOnline.visibility = View.VISIBLE
            online.visibility = View.VISIBLE
            online.visibility = View.VISIBLE
//            searchFriend.isFocusable = true
            noFriends.visibility = View.GONE
        }
    }

    private fun searchFriends(s: String) {

        if (cbOnline.isChecked) {
            newList.forEach {
                if (it.lowerName.startsWith(s)) {
                    list2.add(it)
                }
            }
        } else {
            list.forEach {
                if (it.lowerName.startsWith(s)) {
                    list2.add(it)
                }
            }
        }
        deleteVisibility()
        adapter = FriendsAdapter(context!!, list2, true) { us, pos ->

        }
        try {
            setFriendCount(list2)
            rvFriends.adapter = adapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

                            deleteVisibility()

                            try {
                                setFriendCount(list)
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
            friend.newFriends,
            friend.friends,
            ""
        )
        list.forEach {
            val user2 =
                User(
                    it.id,
                    it.username,
                    it.lowerName,
                    it.email,
                    it.password,
                    it.imageUri,
                    it.newFriends,
                    it.friends,
                    ""
                )
            if (user2 == user) {
                list.remove(it)
                deleteVisibility()
                return true
            }
        }
        return false
    }

    private fun setupRecycler(context: Context) {
        with(rvFriends) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            btnNewFriends -> {
                val fragment = SearchFriendsFragment()
                fragmentManager?.beginTransaction()?.add(R.id.frameLayout, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
            }
            btnAddFriend -> {
                val fragment = NewFriendsFragment()
                fragmentManager?.beginTransaction()?.add(R.id.frameLayout, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
            }
        }
    }
}