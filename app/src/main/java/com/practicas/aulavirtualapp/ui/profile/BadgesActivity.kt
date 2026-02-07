package com.practicas.aulavirtualapp.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicas.aulavirtualapp.R
import com.practicas.aulavirtualapp.adapter.BadgesAdapter
import com.practicas.aulavirtualapp.model.user.BadgeResponse
import com.practicas.aulavirtualapp.repository.AuthRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BadgesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_badges)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbarBadges)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Vistas
        val rvBadges = findViewById<RecyclerView>(R.id.rvBadges)
        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmptyState)
        val loading = findViewById<ProgressBar>(R.id.pbLoadingBadges)

        // Configurar lista
        rvBadges.layoutManager = LinearLayoutManager(this)
        val adapter = BadgesAdapter()
        rvBadges.adapter = adapter

        // Datos del Intent
        val token = intent.getStringExtra("USER_TOKEN") ?: ""
        val userId = intent.getIntExtra("USER_ID", 0)

        // Cargar Datos
        val repository = AuthRepository()

        repository.getUserBadges(token, userId).enqueue(object : Callback<BadgeResponse> {
            override fun onResponse(call: Call<BadgeResponse>, response: Response<BadgeResponse>) {
                loading.visibility = View.GONE
                val badges = response.body()?.badges ?: emptyList()

                if (badges.isNotEmpty()) {
                    rvBadges.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    adapter.updateData(badges)
                } else {
                    rvBadges.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<BadgeResponse>, t: Throwable) {
                loading.visibility = View.GONE
                Toast.makeText(this@BadgesActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }
}