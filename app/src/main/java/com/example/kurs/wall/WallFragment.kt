package com.example.kurs.wall

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kurs.R
import com.example.kurs.comment.Comment
import com.example.kurs.comment.CommentFragment
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
                            if (value != null) {
                                if (value.date.time == post.date.time && post.isLiked) {
                                    referencePosts.child(it.key.toString()).child("users")
                                        .setValue(null)
                                    post.isLiked = false
                                } else
                                    if (value.date.time == post.date.time && !post.isLiked) {
                                        val hashMap = HashMap<String, String>()
                                        hashMap["id"] = currentUser.uid
                                        referencePosts.child(it.key.toString()).child("users")
                                            .push()
                                            .setValue(hashMap)
                                        post.isLiked = true
                                    }
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
        rvWall.adapter = adapter


        referenceFriends.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val id = it.value.toString().replace("{", "").replace("}", "").split("=")[1]
                    friendsIds.add(id)
                }


                getPosts(referencePosts, currentUser)
            }
        })
    }

    private fun getPosts(referencePosts: DatabaseReference, currentUser: FirebaseUser) {
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
                                    if (friendsIds.contains(
                                            post.sender
                                        ) || post.sender == currentUser.uid
                                    ) {
                                        post.senderName = user.username
                                        posts.add(post)
                                        posts.sortByDescending { p -> p.date }
                                        adapter!!.notifyDataSetChanged()
                                    }
                                }
                            }
                        })
                    }

                }

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
                        if (value != null && user != null) {
                            if (value.date.time == post.date.time && value.sender == user.uid) {
                                reference.child(it.key.toString()).setValue(null)
                            }
                        }
                    }
                }
            }
        })

        val referenceComments = FirebaseDatabase.getInstance().getReference("Comments")
        referenceComments.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.children.count() > 0) {
                    p0.children.forEach {
                        val value = it.getValue(Comment::class.java)
                        if (value != null && value.postTime == post.date.time.toString()) {
                            referenceComments.child(it.key.toString()).setValue(null)
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