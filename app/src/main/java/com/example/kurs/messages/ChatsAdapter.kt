package com.example.kurs.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kurs.R
import kotlinx.android.synthetic.main.item_chat.view.*

class ChatsAdapter(val list:ArrayList<Chat>, val clickListener: (Chat, Int) -> Unit) :
    RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Chat = list[position]
        holder.bindItems(item)
        holder.itemView.setOnClickListener { clickListener(item, position) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(chat: Chat) {
            itemView.tvChatName.text = chat.name
            itemView.tvChatLastMessage.text = chat.descr
        }
    }
}
