package de.medieninformatik.mypmaapp.model

/* ────────────────────────────────
 * Kategorien: zentrale Konstanten
 * String-basierte Kategorienamen für UI/DB/Charts.
 * ──────────────────────────────── */

/**
 * Enthält die in der App verwendeten Kategorienamen sowie eine geordnete Liste [ALL]
 * für Filter, Legenden und Diagramme (die Reihenfolge kann visuell relevant sein).
 */
object Category {
    /** Ruhe/Achtsamkeit, z. B. Atemübungen. */
    const val ENTSPANNUNG = "Entspannung"

    /** Kreative Tätigkeiten, z. B. Zeichnen/Skizzieren. */
    const val KREATIVITAET = "Kreativität"

    /** Körperliche Aktivität, z. B. Spaziergang/Übungen. */
    const val BEWEGUNG = "Bewegung"

    /** Kanonische Reihenfolge der Kategorien (für Filter/Legenden/Stacks). */
    val ALL = listOf(ENTSPANNUNG, KREATIVITAET, BEWEGUNG)
}
