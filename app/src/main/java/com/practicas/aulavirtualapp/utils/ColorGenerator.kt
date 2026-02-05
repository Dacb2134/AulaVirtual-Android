package com.practicas.aulavirtualapp

import android.graphics.Color

object ColorGenerator {

    private val colors = listOf(
        "#E1BEE7", // Morado suave
        "#FFCCBC", // Naranja suave
        "#C5CAE9", // Azul índigo suave
        "#B2DFDB", // Verde azulado (Teal)
        "#F0F4C3", // Lima suave
        "#CFD8DC", // Gris azulado
        "#FFAB91", // Coral
        "#81D4FA"  // Azul cielo
    )


    private val iconTints = listOf(
        "#7B1FA2", // Morado fuerte
        "#D84315", // Naranja fuerte
        "#303F9F", // Azul fuerte
        "#00796B", // Verde fuerte
        "#AFB42B", // Lima fuerte
        "#455A64", // Gris fuerte
        "#BF360C", // Coral fuerte
        "#0277BD"  // Azul fuerte
    )

    fun getBackgroundColor(index: Int): Int {
        return Color.parseColor(colors[index % colors.size])
    }

    fun getIconColor(index: Int): Int {
        return Color.parseColor(iconTints[index % iconTints.size])
    }

    fun getIconColorHex(index: Int): String {
        return iconTints[index % iconTints.size]
    }
}