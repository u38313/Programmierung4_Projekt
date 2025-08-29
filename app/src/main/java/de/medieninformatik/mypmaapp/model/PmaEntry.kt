package de.medieninformatik.mypmaapp.model

import androidx.annotation.DrawableRes

/* ────────────────────────────────
 * Domain Model: App-Eintrag/Moment
 * UI-nahes Modell (wird z.B. aus der DB-Entity gemappt) und in Compose verwendet.
 * ──────────────────────────────── */

/**
 * Repräsentiert einen „Moment“/Aktivitätseintrag der App.
 *
 * @property id           Stabile ID des Eintrags (DB/Repository).
 * @property title        Kurzer Titel, einzeilig gedacht.
 * @property description  Optionale/kurze Beschreibung.
 * @property category     Kategoriename als String (kompatibel zu Dropdown/Charts).
 * @property imageRes     Icon/Illustration als Android-Drawable-Ressource.
 * @property timestamp    Erstellzeitpunkt in ms seit Epoch (Standard: „jetzt“).
 */
data class PmaEntry(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    @DrawableRes val imageRes: Int,
    val timestamp: Long = System.currentTimeMillis() // Default beim Anlegen
)
