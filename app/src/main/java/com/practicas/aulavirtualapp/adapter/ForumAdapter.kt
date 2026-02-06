package com.practicas.aulavirtualapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.Forum

class ForumAdapter(
    private var forums: List<Forum> = emptyList(),
    private val onForumClick: (Forum) -> Unit
) : RecyclerView.Adapter<ForumAdapter.ForumViewHolder>() {

    fun updateData(newForums: List<Forum>) {
        forums = newForums
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forum, parent, false)
        return ForumViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
        holder.bind(forums[position])
    }

    override fun getItemCount() = forums.size

    inner class ForumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvForumName)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvForumDesc)
        private val chipCount: TextView = itemView.findViewById(R.id.chipDiscussions)

        fun bind(forum: Forum) {
            tvName.text = forum.name

            // Limpiamos el HTML de la descripción
            if (forum.intro.isNotEmpty()) {
                tvDesc.text = HtmlCompat.fromHtml(forum.intro, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                tvDesc.visibility = View.VISIBLE
            } else {
                tvDesc.visibility = View.GONE
            }

            chipCount.text = "${forum.numDiscussions} debates"

            itemView.setOnClickListener { onForumClick(forum) }
        }
    }
}