package com.example.kurs.comment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kurs.R
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.valdesekamdem.library.mdtoast.MDToast
import kotlinx.android.synthetic.main.fragment_comment.*
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class CommentFragment : Fragment(), View.OnClickListener {

    var postTime = ""
    private var adapter: CommentAdapter? = null
    var comments = ArrayList<Comment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler(requireContext())
        if (arguments != null) {
            postTime = arguments!!.getString("postTime", "")
        }

        val user = FirebaseAuth.getInstance().currentUser!!
        val referenceComments = FirebaseDatabase.getInstance().getReference("Comments")
        val referenceUsers = FirebaseDatabase.getInstance().getReference("Users")
        adapter = CommentAdapter(user.uid, comments) { comment ->
            removeFromComments(referenceComments, comment)
        }
        rvComments.adapter = adapter

        referenceComments.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                comments.clear()
                if (p0.children.count() != 0) {
                    p0.children.forEach {
                        val com = it.getValue(Comment::class.java)
                        if (com != null && com.postTime == postTime) {
                            referenceUsers.child(com.sender)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {
                                        Timber.d(p0.message)
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val us = p0.getValue(User::class.java)
                                        if (us != null) {
                                            val hashMap = HashMap<String, Any>()
                                            hashMap["senderName"] = us.username
                                            referenceUsers.child(com.sender).updateChildren(hashMap)
                                            com.senderName = us.username
                                        }
                                        comments.add(com)
                                    }
                                })
                        }
                    }

                    Handler().postDelayed({
                        try {
                            adapter!!.notifyDataSetChanged()
                            if (comments.isEmpty()) {
                                noComments.visibility = View.VISIBLE
                            } else {
                                noComments.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, 15)
                }
            }
        })
        btnSendComment.setOnClickListener(this)
    }

    private fun removeFromComments(referenceComments: DatabaseReference, comment: Comment) {
        referenceComments.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user = FirebaseAuth.getInstance().currentUser
                if (p0.children.count() > 0) {
                    p0.children.forEach {
                        val value = it.getValue(Comment::class.java)
                        if (value != null && user != null) {
                            if (value.date.time == comment.date.time && value.sender == user.uid) {
                                referenceComments.child(it.key.toString()).setValue(null)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun setupRecycler(context: Context) {
        with(rvComments) {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(false)
        }
    }

    override fun onClick(v: View?) {
        val user = FirebaseAuth.getInstance().currentUser!!
        when (v) {
            btnSendComment -> {

                if (etCommentText.text.toString().isNotEmpty() && postTime != "") {
                    val ref = FirebaseDatabase.getInstance().getReference("Comments")
                    val hashMap = HashMap<String, Any>()
                    hashMap["text"] = etCommentText.text.trim().toString()
                    hashMap["date"] = Date()
                    hashMap["sender"] = user.uid
                    hashMap["postTime"] = postTime
                    ref.push().setValue(hashMap)
                    etCommentText.setText("")
                } else {
                    MDToast.makeText(
                        context,
                        resources.getString(R.string.empty),
                        Toast.LENGTH_LONG,
                        MDToast.TYPE_INFO
                    )
                        .show()
                }
            }
        }
    }
}