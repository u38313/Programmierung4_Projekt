package de.medieninformatik.mypmaapp.data

import de.medieninformatik.mypmaapp.R
import de.medieninformatik.mypmaapp.model.Category
import de.medieninformatik.mypmaapp.model.PmaEntry

/* ─────────────────────────────────────────────────────────────
 * DemoData: kleine Startkollektion für Onboarding/Entwicklung
 * - Wird idempotent über Repository.seedDemoIfMissing() eingefügt
 * - Eindeutigkeit erfolgt per Titel (bitte Titel stabil halten)
 * - Die hier gesetzten IDs sind Platzhalter; die DB vergibt eigene
 *   Primärschlüssel beim Insert.
 * ───────────────────────────────────────────────────────────── */

/**
 * Liefert eine geordnete Liste vordefinierter Momente
 * (für „erste App-Nutzung“ oder Demo-Inhalte).
 */
object DemoData {
    fun initial() = listOf(
        // Entspannung • Icon: mindfulness_24px
        PmaEntry(
            id = 1,
            title = "Atemübung",
            description = "5 bewusste Atemzüge",
            category = Category.ENTSPANNUNG,
            imageRes = R.drawable.mindfulness_24px
        ),

        // Kreativität • Icon: stylus_note_24px
        PmaEntry(
            id = 2,
            title = "Skizze",
            description = "10 Minuten freies Zeichnen",
            category = Category.KREATIVITAET,
            imageRes = R.drawable.stylus_note_24px
        ),

        // Bewegung • Icon: nature_people_24px
        PmaEntry(
            id = 3,
            title = "Kurzspaziergang",
            description = "10 Min. ohne Handy",
            category = Category.BEWEGUNG,
            imageRes = R.drawable.nature_people_24px
        )
    )
}
