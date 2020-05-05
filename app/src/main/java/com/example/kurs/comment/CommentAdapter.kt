package com.example.kurs.comment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kurs.R
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentAdapter(
    val currentId: String,
    val list: ArrayList<Comment>,
    val deleteListener: (Comment) -> Unit
) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Comment = list[position]
        holder.itemView.ivCommentDelete.setOnClickListener { deleteListener(item) }
        holder.bindItems(item, currentId)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(post: Comment, currentId: String) {
            itemView.tvCommentSender.text = post.senderName
            itemView.tvCommentText.text = post.text
            if (currentId == post.sender) {
                itemView.ivCommentDelete.visibility = View.VISIBLE
            }
        }
    }
}
