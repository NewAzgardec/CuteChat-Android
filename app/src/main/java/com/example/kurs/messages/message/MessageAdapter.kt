package com.example.kurs.messages.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kurs.R
import kotlinx.android.synthetic.main.item_message.view.*

class MessageAdapter(val list:ArrayList<Message>, val clickListener: (Message, Int) -> Unit) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Message = list[position]
        holder.bindItems(item)
        holder.itemView.setOnClickListener { clickListener(item, position) }
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
}
