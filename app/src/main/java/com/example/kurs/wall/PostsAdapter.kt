package com.example.kurs.wall

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kurs.R
import kotlinx.android.synthetic.main.item_post.view.*

class PostsAdapter(
    val currentId: String,
    val context: Context,
    val list: ArrayList<Post>,
    val deleteListener: (Post) -> Unit,
    val likeListener: (Post) -> Unit,
    val commentListener: (Post) -> Unit
) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Post = list[position]
        if(item.users!=null){
            item.users.forEach {it1->
                item.isLiked = !item.isLiked&&it1.value.toString().replace("id ->","").contains(currentId)
            }
        }

        holder.itemView.ivDelete.setOnClickListener { deleteListener(item) }
        holder.itemView.ivLike.setOnClickListener {
            likeListener(item)
        }
        holder.itemView.btnComment.setOnClickListener {
            commentListener(item)
        }
        holder.bindItems(item, currentId, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(post: Post, currentId: String, context: Context) {
            itemView.tvSender.text = post.senderName
            itemView.tvText.text = post.text
            if(post.users!=null){
                itemView.tvCount.text = post.users.size.toString()
            }else{
                itemView.tvCount.text = "0"
            }
            if (currentId == post.sender) {
                itemView.ivDelete.visibility = View.VISIBLE
            }
            if(post.isLiked){
                itemView.ivLike.isChecked = true
            }

            if(post.uri!=null&&post.uri!=""){
                Glide.with(context)
                    .load(post.uri)
                    .into(itemView.ivPost)
            }else{
                itemView.ivPost.visibility = View.GONE
            }
        }
    }
}
