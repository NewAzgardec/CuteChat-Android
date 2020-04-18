package com.example.kurs.wall

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
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_wall.*
import timber.log.Timber

class WallFragment : Fragment() {
    private var adapter: PostsAdapter? = null
    var posts = ArrayList<Post>()
    var friendsIds = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wall, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler(requireContext())
        val currentUser = FirebaseAuth.getInstance().currentUser
        val referencePosts = FirebaseDatabase.getInstance().getReference("Wall")
        val referenceFriends =
            FirebaseDatabase.getInstance().getReference("Users").child(currentUser!!.uid)
                .child("friends")

        adapter = PostsAdapter(currentUser.uid, context!!, posts, { post ->
            //TODO delete post
            removeFromPosts(referencePosts, post)
        }, { post ->
            referencePosts.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Timber.d(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.children.count() > 0) {
                        p0.children.forEach {
                            val value = it.getValue(Post::class.java)
                            if(value!=null) {
                                if (value.id == post.id && post.isLiked) {
                                    referencePosts.child(it.key.toString()).child("users").setValue(null)
                                }else
                                if (value.id == post.id && !post.isLiked) {
                                    val hashMap = HashMap<String, String>()
                                    hashMap["id"] = currentUser.uid
                                    referencePosts.child(it.key.toString()).child("users").push()
                                        .setValue(hashMap)
                                }
                                adapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                }
            })
        })

        referenceFriends.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val id = it.value.toString().replace("{", "").replace("}", "").split("=")[1]
                    friendsIds.add(id)
                }
                referencePosts.addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Timber.d(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        posts.clear()
                        p0.children.forEach {
                            val post = it.getValue(Post::class.java)
                            if (post != null) {
                                val ref = FirebaseDatabase.getInstance().getReference("Users")
                                    .child(post.sender)
                                ref.addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        Timber.d(p0.message)
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(User::class.java)
                                        if (user != null) {
                                            if (post.sender == currentUser.uid || friendsIds.contains(
                                                    post.sender
                                                )
                                            ) {
                                                //TODO add sender name
//                                                post.sender = user.username
                                                posts.add(post)
                                            }
                                        }
                                        try {
                                            posts.sortByDescending { it.date }
                                            rvWall.adapter = adapter
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                })
                            }
                        }
                    }
                })
            }

        })
    }

    private fun removeFromPosts(reference: DatabaseReference, post: Post) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user = FirebaseAuth.getInstance().currentUser
                if (p0.children.count() > 0) {
                    p0.children.forEach {
                        val value = it.getValue(Post::class.java)
                        if (value != null&&user!=null) {
                            if (value.uri == post.uri&&value.sender==user.uid) {
                                reference.child(it.key.toString()).setValue(null)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun setupRecycler(context: Context) {
        with(rvWall) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
    }
}