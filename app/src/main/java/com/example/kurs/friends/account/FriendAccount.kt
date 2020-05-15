package com.example.kurs.friends.account

import android.Manifest.permission.CALL_PHONE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.kurs.R
import com.example.kurs.comment.CommentFragment
import com.example.kurs.profile.Phone
import com.example.kurs.profile.User
import com.example.kurs.wall.Post
import com.example.kurs.wall.PostsAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.layout_friend_account.*
import timber.log.Timber

class FriendAccount : Fragment(), View.OnClickListener {
    private var adapter: PostsAdapter? = null
    var posts = ArrayList<Post>()
    var userName = ""
    var friendId = ""
    var isExists = false
    val phoneList = ArrayList<Phone>()
    var phoneViews = ArrayList<View>()
    var selectedPhone = Phone()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friend_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler(requireContext())

        if (arguments != null) {
            friendId = arguments!!.getString("friendId", "")
            isExists = arguments!!.getBoolean("isExists")
        }

        if (isExists) {
            removeFromFriends.visibility = View.VISIBLE
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users").child(friendId)
        val referencePosts = FirebaseDatabase.getInstance().getReference("Wall")
        val referencePhones = FirebaseDatabase.getInstance().getReference("Phones")

        referencePhones.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.children.count() > 0) {
                    p0.children.forEach {
                        val phone = it.getValue(Phone::class.java)
                        if (!phoneList.contains(phone) && phone != null && friendId != "" && phone.owner == friendId) {
                            phoneList.add(phone)
                            addPhoneView(phone)
                        }
                        try {
                            if (phoneList.isNotEmpty()) {
                                showPhones.visibility = View.VISIBLE
                            } else {
                                showPhones.visibility = View.GONE
                            }
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
        val us = FirebaseAuth.getInstance().currentUser

        adapter = PostsAdapter(us?.uid ?: "", context!!, posts, {
        }, { post ->
            referencePosts.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Timber.d(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.children.count() > 0) {
                        p0.children.forEach {
                            val value = it.getValue(Post::class.java)
                            if (value != null) {
                                if (value.date.time == post.date.time && post.isLiked) {
                                    referencePosts.child(it.key.toString()).child("users")
                                        .setValue(null)
                                    post.isLiked = false
                                } else
                                    if (value.date.time == post.date.time && !post.isLiked) {
                                        val hashMap = HashMap<String, String>()
                                        hashMap["id"] = us?.uid ?: ""
                                        referencePosts.child(it.key.toString()).child("users")
                                            .push()
                                            .setValue(hashMap)
                                        post.isLiked = true
                                    }
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                }
            })
        }, { post ->
            val fragment = CommentFragment()
            val args = Bundle()
            args.putString("postTime", post.date.time.toString())
            fragment.arguments = args
            fragmentManager?.beginTransaction()?.add(R.id.frameLayout, fragment)
                ?.addToBackStack(null)
                ?.commit()
        })


        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user2 = p0.getValue(User::class.java)
                if (user2 != null) {
                    try {
                        name.text = user2.username
                        userEmail.text = user2.email
                        userName = user2.username
                        if (user2.imageUri != "") {
                            Glide.with(context!!).load(user2.imageUri).into(ivAva)
                        } else {
                            Glide.with(context!!).load(R.drawable.ava).into(ivAva)
                        }
                        pbUsername.visibility = View.GONE
                        getPosts(referencePosts, friendId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })

        removeFromFriends.setOnClickListener(this)
        showPhones.setOnClickListener(this)
    }

    private fun addPhoneView(phone: Phone) {
        try {
            val view = LayoutInflater.from(context).inflate(R.layout.item_call, null, false)
            view.findViewById<ImageButton>(R.id.btnCallPhone).setOnClickListener {
                selectedPhone = phone
                if (!checkSelfPermission()) {
                    requestPermission()
                } else {
                    startCall(selectedPhone)
                }
            }
            phoneViews.add(view)
            view.findViewById<EditText>(R.id.etPhoneNumber).setText(phone.phone)
            ltUserPhones.addView(view)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun startCall(phone: Phone) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + phone.phone)
        startActivity(intent)
    }

    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(CALL_PHONE), 3)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 3) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCall(selectedPhone)
            }
        }
    }

    private fun getPosts(referencePosts: DatabaseReference, userId: String?) {
        referencePosts.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                posts.clear()
                if (p0.children.count() == 0) {
                    pbPosts.visibility = View.GONE
                    noPosts.visibility = View.VISIBLE
                    view1.visibility = View.GONE
                    postsTitle.visibility = View.GONE
                    view2.visibility = View.GONE
                } else {
                    p0.children.forEach {
                        val post = it.getValue(Post::class.java)
                        if (post != null && post.sender == userId ?: "") {
                            if (userName != "") {
                                post.senderName = userName
                            }
                            posts.add(post)
                            posts.sortByDescending { p -> p.date }
                        }
                    }
                    try {
                        if (posts.isEmpty()) {
                            pbPosts.visibility = View.GONE
                            noPosts.visibility = View.VISIBLE
                            view1.visibility = View.GONE
                            postsTitle.visibility = View.GONE
                            view2.visibility = View.GONE
                        } else {
                            noPosts.visibility = View.GONE
                            view1.visibility = View.VISIBLE
                            postsTitle.visibility = View.VISIBLE
                            view2.visibility = View.VISIBLE
                        }

                        adapter!!.notifyDataSetChanged()
                        pbPosts.visibility = View.GONE
                        rvPosts.isNestedScrollingEnabled = false
                        rvPosts.adapter = adapter
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun setupRecycler(context: Context) {
        with(rvPosts) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
    }

    override fun onClick(p0: View?) {
        when (p0) {
            removeFromFriends -> {
                val referenceCurrent = FirebaseDatabase.getInstance().getReference("Users")
                val currentFriendsRef =
                    referenceCurrent.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child("friends")

                val friendRef =
                    referenceCurrent.child(friendId)
                        .child("friends")

                currentFriendsRef.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Timber.d(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.children.count() > 0) {
                            p0.children.forEach {
                                val value = it.value.toString().replace("}", "").split("=")[1]
                                if (value == friendId) {
                                    currentFriendsRef.child(it.key.toString()).setValue(null)
                                }
                            }
                        }
                    }
                })

                friendRef.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Timber.d(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.children.count() > 0) {
                            p0.children.forEach {
                                val value = it.value.toString().replace("}", "").split("=")[1]
                                if (value == FirebaseAuth.getInstance().currentUser!!.uid) {
                                    friendRef.child(it.key.toString()).setValue(null)
                                    try {
                                        removeFromFriends.visibility = View.GONE
                                    }catch (e: Exception){
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                })
            }

            showPhones -> {
                if (showPhones.text == resources.getString(R.string.call)) {
                    showPhones.text = resources.getString(R.string.hide)
                    ltUserPhones.visibility = View.VISIBLE
                } else {
                    showPhones.text = resources.getString(R.string.call)
                    ltUserPhones.visibility = View.GONE
                }
            }
        }
    }
}