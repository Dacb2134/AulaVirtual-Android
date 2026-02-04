package com.practicas.aulavirtualapp.utils

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.practicas.aulavirtualapp.R

// Función de extensión para configurar colores automáticamente
fun SwipeRefreshLayout.setupBrandColors() {
    setColorSchemeResources(R.color.primary, R.color.secondary)
}