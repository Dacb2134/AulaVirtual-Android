package com.practicas.aulavirtualapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.user.Badge
import java.text.SimpleDateFormat
import java.util.*

class BadgesAdapter(private var badges: List<Badge> = emptyList()) :
    RecyclerView.Adapter<BadgesAdapter.BadgeViewHolder>() {

    fun updateData(newBadges: List<Badge>) {
        this.badges = newBadges
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(badges[position])
    }

    override fun getItemCount() = badges.size

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivBadgeIcon)
        private val tvName: TextView = itemView.findViewById(R.id.tvBadgeName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvBadgeDate)

        fun bind(badge: Badge) {
            // Nombre real de la medalla
            tvName.text = badge.name

            // Fecha real
            val date = Date(badge.dateIssued * 1000)
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvDate.text = "Obtenida: ${format.format(date)}"

            // Cargar imagen REAL desde la URL de Moodle usando Glide
            Glide.with(itemView.context)
                .load(badge.badgeUrl) // <--- URL dinámica
                .placeholder(android.R.drawable.star_big_on) // Mientras carga
                .error(android.R.drawable.ic_delete) // Si falla
                .into(ivIcon)
        }
    }
}