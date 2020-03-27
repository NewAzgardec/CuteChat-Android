package com.example.kurs.wall

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kurs.R
import kotlinx.android.synthetic.main.item_chat.view.*
import kotlinx.android.synthetic.main.item_post.view.*

class PostsAdapter(val currentId:String, val context: Context, val list:ArrayList<Post>,  val deleteListener: (Post) -> Unit) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Post = list[position]
        holder.bindItems(item, currentId, context)
        holder.itemView.ivDelete.setOnClickListener { deleteListener(item) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(post: Post,currentId:String,  context:Context) {
            itemView.tvSender.text = post.sender
            itemView.tvText.text = post.text
            itemView.tvCount.text = post.likes.toString()
            if(currentId==post.sender){
                itemView.ivDelete.visibility = View.VISIBLE
            }
            Glide.with(context)
                .load(post.uri)
                .into(itemView.ivPost)
        }
    }
}
