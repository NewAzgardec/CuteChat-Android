package com.example.kurs.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import kotlinx.android.synthetic.main.item_friend.view.*
import timber.log.Timber

class FriendsAdapter(val context: Context, val list:ArrayList<User>,  val friends:Boolean, val clickListener: (User, Int) -> Unit) :
    RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    private var lastMessage = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val l = list.sortedBy {l-> l.lowerName }
        val item: User = l[position]
        if(!friends){
            holder.itemView.tvLastMessage.visibility = View.VISIBLE
            getLastMessage(item.id, holder.itemView.tvLastMessage)
        }

        holder.bindItems(item, context)
        holder.itemView.setOnClickListener { clickListener(item, position) }
    }

    private fun getLastMessage(id: String, tvLastMessage: TextView?) {
        val user  = FirebaseAuth.getInstance().currentUser
        val reference  = FirebaseDatabase.getInstance().getReference("Messages")
        reference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    val chat = it.getValue(Message::class.java)
                    if (chat != null&&user!=null&&tvLastMessage!=null) {
                        if(chat.receiverId==user.uid&&chat.senderId==id||chat.receiverId==id&&chat.senderId==user.uid){
                            lastMessage = chat.text
                            tvLastMessage.text = lastMessage
                        }
                    }
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: User, context: Context) {
            if(user.onlineStatus=="true"){
                itemView.ivIsOnline.visibility = View.VISIBLE
            }
            itemView.tvName.text = user.username

           if(user.imageUri!=null&&user.imageUri.isNotEmpty())  Glide.with(context).load(user.imageUri).into(itemView.ivPhoto)
        }
    }
}
