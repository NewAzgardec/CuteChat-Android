package com.example.kurs.messages.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kurs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.item_message_right.view.*

class MessageAdapter(val list:ArrayList<Message>, val clickListener: (Message, Int) -> Unit) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private val TYPE_LEFT=0
    private val TYPE_RIGHT=1
    lateinit var user:FirebaseUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType==TYPE_RIGHT) {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_right, parent, false)
            ViewHolder(v)
        }else{
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_message_left, parent, false)
            ViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Message = list[position]
        holder.bindItems(item)
        holder.itemView.setOnClickListener { clickListener(item, position) }
        if(position ==list.size-1){
            if(item.seen){
                holder.itemView.tvSeen.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(message: Message) {
            itemView.tvSender.text = message.senderName
            itemView.tvMessage.text = message.text
            itemView.tvDate.text = message.date
        }
    }

    override fun getItemViewType(position: Int): Int {
        user = FirebaseAuth.getInstance().currentUser!!
        return if(list[position].senderId==user.uid){
            TYPE_RIGHT
        }else{
            TYPE_LEFT
        }
    }
}
