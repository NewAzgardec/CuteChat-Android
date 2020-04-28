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
import kotlinx.android.synthetic.main.item_friend.view.tvName
import kotlinx.android.synthetic.main.item_new_friend.view.*

class NewFriendsAdapter(val context: Context, val list:ArrayList<User>, val clickListener: (User, Int) -> Unit, val clickListener2: (User, Int) -> Unit, val clickListener3: (User, Int) -> Unit) :
    RecyclerView.Adapter<NewFriendsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_new_friend, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: User = list[position]
        holder.bindItems(item, context)
        holder.itemView.setOnClickListener { clickListener(item, position) }
        holder.itemView.accept.setOnClickListener { clickListener2(item, position) }
        holder.itemView.decline.setOnClickListener { clickListener3(item, position) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: User, context: Context) {
            itemView.tvName.text = user.username

            if(user.imageUri!=null&&user.imageUri!=""){
                Glide.with(context)
                    .load(user)
                    .into(itemView.ivNewPhoto)
            }else{
                Glide.with(context)
                    .load(ContextCompat.getDrawable(context, R.drawable.ava))
                    .into(itemView.ivNewPhoto)
            }
        }
    }
}
