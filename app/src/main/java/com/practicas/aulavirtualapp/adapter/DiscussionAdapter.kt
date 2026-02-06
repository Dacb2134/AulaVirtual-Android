package com.practicas.aulavirtualapp.adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.model.ForumDiscussion
import java.text.SimpleDateFormat
import java.util.*

class DiscussionAdapter(
    private var discussions: List<ForumDiscussion> = emptyList(),
    // Callback modificado: recibe el item y opcionalmente el mensaje de respuesta
    private val onAction: (ForumDiscussion, String?) -> Unit
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
        // Vistas existentes
        private val tvAuthor: TextView = view.findViewById(R.id.tvAuthorName)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessageContent)
        private val tvReplies: TextView = view.findViewById(R.id.tvReplyCount)
        private val ivAuthor: ImageView = view.findViewById(R.id.ivAuthor)

        // Vistas NUEVAS para responder (Deben estar en tu item_discussion.xml)
        private val btnReply: TextView? = view.findViewById(R.id.btnReply)
        private val layoutReplyInput: LinearLayout? = view.findViewById(R.id.layoutReplyInput)
        private val etReplyMessage: EditText? = view.findViewById(R.id.etReplyMessage)
        private val btnSendReply: Button? = view.findViewById(R.id.btnSendReply)
        private val btnCancelReply: TextView? = view.findViewById(R.id.btnCancelReply)

        fun bind(item: ForumDiscussion) {
            // Lógica de visualización básica
            tvAuthor.text = item.userFullName
            tvSubject.text = item.name

            val date = Date(item.created * 1000L)
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvDate.text = format.format(date)

            if (item.message.isNotEmpty()) {
                val cleanText = HtmlCompat.fromHtml(item.message, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvMessage.text = cleanText.toString().trim()
                tvMessage.visibility = View.VISIBLE
            } else {
                tvMessage.visibility = View.GONE
            }

            tvReplies.text = if (item.numReplies == 1) "1 respuesta" else "${item.numReplies} respuestas"

            // Color del avatar
            // Nota: Si ColorGenerator no es un object singleton en tu proyecto, ajusta esto.
            // Asumo que tienes una clase utils/ColorGenerator.kt
            try {
                val colorIndex = item.userFullName.hashCode()
                // Ajusta la llamada a tu ColorGenerator real si es diferente
                val color = com.practicas.aulavirtualapp.ColorGenerator.getBackgroundColor(Math.abs(colorIndex))
                ivAuthor.background.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            } catch (e: Exception) {
                // Fallback si la clase utils no está lista
            }

            // Click general en la tarjeta (Navegar al detalle)
            itemView.setOnClickListener {
                onAction(item, null)
            }

            // --- LÓGICA DE RESPUESTA ---
            // Solo configuramos si las vistas existen en el XML
            if (btnReply != null && layoutReplyInput != null) {

                // Botón "Responder" -> Muestra/Oculta el input
                btnReply.setOnClickListener {
                    if (layoutReplyInput.visibility == View.VISIBLE) {
                        layoutReplyInput.visibility = View.GONE
                    } else {
                        layoutReplyInput.visibility = View.VISIBLE
                        etReplyMessage?.requestFocus()
                    }
                }

                // Botón "Cancelar" -> Limpia y oculta
                btnCancelReply?.setOnClickListener {
                    layoutReplyInput.visibility = View.GONE
                    etReplyMessage?.text?.clear()
                }

                // Botón "Enviar" -> Llama al callback con el mensaje
                btnSendReply?.setOnClickListener {
                    val message = etReplyMessage?.text.toString().trim()
                    if (message.isNotEmpty()) {
                        // Enviamos la acción con mensaje
                        onAction(item, message)

                        // Ocultamos teclado y input preventivamente
                        layoutReplyInput.visibility = View.GONE
                        etReplyMessage?.text?.clear()
                    }
                }
            }
        }
    }
}