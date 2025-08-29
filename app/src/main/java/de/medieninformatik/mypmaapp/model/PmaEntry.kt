package de.medieninformatik.mypmaapp.model

import androidx.annotation.DrawableRes


/*
 * Repräsentiert einen Aktivitätseintrag
 */
data class PmaEntry(
    val id: Long,   //ID
    val title: String,  //Titel
    val description: String,    //Beschreibung
    val category: String,   //Kategorie
    @DrawableRes val imageRes: Int, //Icon ID
    val timestamp: Long = System.currentTimeMillis() // Zeitstempel
)
