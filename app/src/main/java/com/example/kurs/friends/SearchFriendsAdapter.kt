package com.example.kurs.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kurs.R
import com.example.kurs.messages.message.Message
import com.example.kurs.profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.item_friend.view.ivIsOnline
import kotlinx.android.synthetic.main.item_friend.view.ivPhoto
import kotlinx.android.synthetic.main.item_friend.view.tvLastMessage
import kotlinx.android.synthetic.main.item_friend.view.tvName
import kotlinx.android.synthetic.main.item_friend_add.view.*
import timber.log.Timber

class SearchFriendsAdapter(
    val context: Context,
    val list: ArrayList<User>,
    val friends: Boolean,
    val clickListener: (User, Int) -> Unit,
    val clickAcceptListener: (User, Int) -> Unit
) :
    RecyclerView.Adapter<SearchFriendsAdapter.ViewHolder>() {

    private var lastMessage = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_add, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val l = list.sortedBy { l -> l.lowerName }
        val item: User = l[position]
        if (!friends) {
            holder.itemView.tvLastMessage.visibility = View.VISIBLE
            getLastMessage(item.id, holder.itemView.tvLastMessage)
        }
        checkIfExistsNewFriends(friends, holder, item)

        holder.itemView.btnSendRequest.setOnClickListener { clickListener(item, position) }

        holder.itemView.btnAccept.setOnClickListener { clickAcceptListener(item, position) }
    }

    private fun getLastMessage(id: String, tvLastMessage: TextView?) {
        val user = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Messages")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val chat = it.getValue(Message::class.java)
                    if (chat != null && user != null && tvLastMessage != null) {
                        if (chat.receiverId == user.uid && chat.senderId == id || chat.receiverId == id && chat.senderId == user.uid) {
                            lastMessage = chat.text
                            tvLastMessage.text = lastMessage
                        }
                    }
                }
            }
        })
    }

    private fun checkIfExistsNewFriends(isFriends: Boolean, holder: ViewHolder, item: User) {
        if (isFriends) {
            val user = FirebaseAuth.getInstance().currentUser
            val reference = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid)
            reference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Timber.d(p0.message)
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val cur = p0.getValue(User::class.java)
                    val newFriends =
                       cur?.newFriends
                    val o = ArrayList<String>()
                    newFriends?.forEach {
                        val value = it.value.toString().replace("{", "").replace("}", "").split("=")[1]
                        o.add(value)
                    }
                    list.forEach {
                        if (o != null) {
                            if (o.contains(it.id)) holder.bindItems(it, context, true)
                            else holder.bindItems(item, context, false)
                        }

                    }

                    Timber.d(newFriends.toString())

                }
            })
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: User, context: Context, isExists: Boolean) {
            if (user.onlineStatus == "true") {
                itemView.ivIsOnline.visibility = View.VISIBLE
            }
            itemView.tvName.text = user.username

            if (user.imageUri != null && user.imageUri.isNotEmpty()) Glide.with(context).load(user.imageUri).into(
                itemView.ivPhoto
            )
            else Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.ava)).into(
                itemView.ivPhoto
            )

            if (isExists) {
                itemView.btnSendRequest.visibility = View.GONE
                itemView.btnAccept.visibility = View.VISIBLE
            }
        }
    }
}
