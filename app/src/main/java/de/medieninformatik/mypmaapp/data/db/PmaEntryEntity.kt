package de.medieninformatik.mypmaapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/* ─────────────────────────────────────────────────────────────
 * PmaEntryEntity
 * Room-Entity für einen Moment/Eintrag, wie er in der DB gespeichert wird.
 * - Primärschlüssel wird von Room automatisch vergeben.
 * - timestamp wird clientseitig beim Erstellen gesetzt (Millis seit Epoch).
 * ───────────────────────────────────────────────────────────── */

@Entity(tableName = "entries")
data class PmaEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                  // DB-ID (autogeneriert)

    val title: String,                 // Titel des Moments
    val description: String,           // Beschreibung/Details
    val category: String,              // Kategorie (z. B. Entspannung)
    val imageRes: Int,                 // Drawable-ResId für das Karten-Icon

    val timestamp: Long = System.currentTimeMillis() // Erstellzeitpunkt (ms)
)
