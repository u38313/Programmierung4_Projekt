package de.medieninformatik.mypmaapp.model

import androidx.annotation.DrawableRes

    data class PmaEntry(
        val id: Long,
        val title: String,
        val description: String,
        val category: String,
        @DrawableRes val imageRes: Int,
        val timestamp: Long = System.currentTimeMillis()
    )