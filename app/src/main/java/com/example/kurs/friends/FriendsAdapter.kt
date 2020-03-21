package com.example.kurs.friends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kurs.R
import com.example.kurs.profile.User
import kotlinx.android.synthetic.main.item_friend.view.*

class FriendsAdapter(val context: Context, val list:ArrayList<User>, val clickListener: (User, Int) -> Unit) :
    RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: User = list[position]
        holder.bindItems(item, context)
        holder.itemView.setOnClickListener { clickListener(item, position) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: User, context: Context) {
            if(user.onlineStatus=="online"){
                itemView.ivIsOnline.visibility = View.VISIBLE
            }
            itemView.tvName.text = user.username
        }
    }
}
