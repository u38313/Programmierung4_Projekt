package de.medieninformatik.mypmaapp.data

import de.medieninformatik.mypmaapp.R
import de.medieninformatik.mypmaapp.model.Category
import de.medieninformatik.mypmaapp.model.PmaEntry

/*
* vordefinierte Aktivitäten beim ersten Starten der App
* */

object DemoData {
    fun initial() = listOf(
        // Entspannung
        PmaEntry(
            id = 1,
            title = "Atemübung",
            description = "1 Minute bewusst Atmen",
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
            description = "10 Minuten an der frischen Luft",
            category = Category.BEWEGUNG,
            imageRes = R.drawable.nature_people_24px
        )
    )
}
