package com.practicas.aulavirtualapp.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel
    private var currentSiteUrl: String = "http://192.168.1.144"
    private var userToken: String = ""
    private var currentUserId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- REFERENCIAS UI ---
        val tvFullname = view.findViewById<TextView>(R.id.tvFullname)
        val tvEmail = view.findViewById<TextView>(R.id.tvEmail)
        val tvLocation = view.findViewById<TextView>(R.id.tvLocation)
        val ivProfilePic = view.findViewById<ImageView>(R.id.ivProfilePic)
        val tvRole = view.findViewById<TextView>(R.id.tvRole) // La cinta

        // Académico
        val tvInstitution = view.findViewById<TextView>(R.id.tvInstitution)
        val tvDepartment = view.findViewById<TextView>(R.id.tvDepartment)

        // Contacto y Bio
        val tvPhone = view.findViewById<TextView>(R.id.tvPhone)
        val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)

        // UI General
        val tvBadgeCount = view.findViewById<TextView>(R.id.tvBadgeCount)
        val tvFileCount = view.findViewById<TextView>(R.id.tvFileCount)
        val pbLoading = view.findViewById<ProgressBar>(R.id.pbLoading)
        val btnOpenWeb = view.findViewById<Button>(R.id.btnOpenWeb)
        val btnSettings = view.findViewById<ImageButton>(R.id.btnSettings)
        val containerBadges = view.findViewById<LinearLayout>(R.id.containerBadges)

        // Datos del Intent
        userToken = requireActivity().intent.getStringExtra("USER_TOKEN") ?: ""
        currentUserId = requireActivity().intent.getIntExtra("USER_ID", 0)

        // --- LISTENERS ---
        ivProfilePic.setOnClickListener { mostrarOpcionesFoto() }
        btnOpenWeb.setOnClickListener { abrirMoodleWeb("/user/edit.php?id=$currentUserId") }
        btnSettings.setOnClickListener { startActivity(Intent(requireContext(), SettingsActivity::class.java)) }
        containerBadges.setOnClickListener {
            val intent = Intent(requireContext(), BadgesActivity::class.java)
            intent.putExtra("USER_TOKEN", userToken)
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        // --- VIEWMODEL ---
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // 1. OBSERVAR ROL (Nueva lógica segura)
        viewModel.userRole.observe(viewLifecycleOwner) { rol ->
            tvRole.text = rol // Se actualiza automáticamente a ADMIN o ESTUDIANTE
        }

        // 2. OBSERVAR DATOS DE PERFIL
        viewModel.userDetail.observe(viewLifecycleOwner) { user ->
            tvFullname.text = user.fullname
            tvEmail.text = user.email

            // Ubicación
            val parts = listOfNotNull(user.city, user.country).filter { it.isNotEmpty() }
            tvLocation.text = if (parts.isNotEmpty()) parts.joinToString(", ") else "Ubicación no disponible"

            // Académico
            tvInstitution.text = if (!user.institution.isNullOrEmpty()) user.institution else "No registrada"
            tvDepartment.text = if (!user.department.isNullOrEmpty()) user.department else "Estudiante"

            // Contacto (Recuperado)
            val phoneInfo = listOfNotNull(user.phone, user.mobile).filter { it.isNotEmpty() }.joinToString(" / ")
            tvPhone.text = if (phoneInfo.isNotEmpty()) phoneInfo else "No registrado"
            tvAddress.text = if (!user.address.isNullOrEmpty()) user.address else "No registrada"

            // Descripción (Recuperado)
            if (!user.description.isNullOrEmpty()) {
                val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(user.description, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(user.description)
                }
                tvDescription.text = spanned
            } else {
                tvDescription.text = "¡Hola! Soy estudiante en esta plataforma."
            }

            // Imagen
            val imageUrlWithToken = "${user.profileImageUrl}?token=$userToken"
            Glide.with(this)
                .load(imageUrlWithToken)
                .placeholder(android.R.drawable.ic_menu_myplaces)
                .error(android.R.drawable.ic_lock_lock)
                .centerCrop()
                .into(ivProfilePic)
        }

        // 3. EXTRAS
        viewModel.badges.observe(viewLifecycleOwner) { tvBadgeCount.text = it.size.toString() }
        viewModel.files.observe(viewLifecycleOwner) { tvFileCount.text = it.size.toString() }
        viewModel.cargando.observe(viewLifecycleOwner) { pbLoading.visibility = if (it) View.VISIBLE else View.GONE }


        if (userToken.isNotEmpty()) {
            viewModel.cargarPerfilCompleto(userToken, currentUserId)
        }
    }

    private fun mostrarOpcionesFoto() {
        val opciones = arrayOf("Ver foto completa", "Cambiar foto (Web)")
        AlertDialog.Builder(requireContext())
            .setTitle("Foto de Perfil")
            .setItems(opciones) { _, which ->
                if (which == 1) abrirMoodleWeb("/user/edit.php?id=$currentUserId")
                else Toast.makeText(context, "Mostrando...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun abrirMoodleWeb(path: String) {
        val url = if (currentSiteUrl.endsWith("/")) currentSiteUrl.dropLast(1) + path else currentSiteUrl + path
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}