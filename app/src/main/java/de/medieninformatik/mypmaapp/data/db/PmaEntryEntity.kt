package de.medieninformatik.mypmaapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * Aktivitätseintrag Entity für die Datenbank
 */
@Entity(tableName = "entries")
data class PmaEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                  // Primärschlüssel ID

    val title: String,                 // Titel
    val description: String,           // Beschreibung
    val category: String,              // Kategorie
    val imageRes: Int,                 // Icon ID

    val timestamp: Long = System.currentTimeMillis() // Zeitstempel
)
