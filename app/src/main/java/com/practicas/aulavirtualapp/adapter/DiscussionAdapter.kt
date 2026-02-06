package com.practicas.aulavirtualapp.adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.ColorGenerator
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.ForumDiscussion

import java.text.SimpleDateFormat
import java.util.*

class DiscussionAdapter(
    private var discussions: List<ForumDiscussion> = emptyList(),
    private val onClick: (ForumDiscussion) -> Unit
) : RecyclerView.Adapter<DiscussionAdapter.ViewHolder>() {

    fun updateData(newDiscussions: List<ForumDiscussion>) {
        discussions = newDiscussions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_discussion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(discussions[position])
    }

    override fun getItemCount() = discussions.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Referencias a los nuevos IDs del XML rediseñado
        private val tvAuthor: TextView = view.findViewById(R.id.tvAuthorName)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessageContent)
        private val tvReplies: TextView = view.findViewById(R.id.tvReplyCount)
        private val ivAuthor: ImageView = view.findViewById(R.id.ivAuthor)

        fun bind(item: ForumDiscussion) {
            // 1. Datos básicos
            tvAuthor.text = item.userFullName
            tvSubject.text = item.name

            // 2. Formato de Fecha (Ej: 05 Feb 2026, 14:30)
            val date = Date(item.created * 1000L)
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvDate.text = format.format(date)

            // 3. Renderizar HTML del mensaje (Lo más importante para que se vea el contenido)
            if (item.message.isNotEmpty()) {
                // FROM_HTML_MODE_LEGACY es compatible con todas las versiones de Android
                val cleanText = HtmlCompat.fromHtml(item.message, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvMessage.text = cleanText.toString().trim() // trim() quita saltos de línea extra al final
                tvMessage.visibility = View.VISIBLE
            } else {
                tvMessage.visibility = View.GONE
            }

            // 4. Contador de respuestas
            tvReplies.text = if (item.numReplies == 1) "1 respuesta" else "${item.numReplies} respuestas"

            // 5. Avatar con Color Dinámico (Usando tu Utils/ColorGenerator)
            // Usamos el ID o el nombre para generar un color consistente
            val colorIndex = item.userFullName.hashCode()
            val color = ColorGenerator.getBackgroundColor(Math.abs(colorIndex))

            // Teñimos el círculo de fondo con el color generado
            ivAuthor.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)

            // Listener para abrir el hilo completo
            itemView.setOnClickListener { onClick(item) }
        }
    }
}