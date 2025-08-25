package de.medieninformatik.mypmaapp.data

import de.medieninformatik.mypmaapp.model.PmaEntry
import de.medieninformatik.mypmaapp.R

object DemoData {
    fun initial(): List<PmaEntry> = listOf(
        PmaEntry(1, "Tief geatmet", "5 bewusste Atemzüge", "Atmung", R.drawable.placeholder_entry),
        PmaEntry(2, "Kurzspaziergang", "10 Min. ohne Handy", "Natur", R.drawable.placeholder_entry),
        PmaEntry(3, "Digital Detox", "Abends kein Social Media", "Digital", R.drawable.placeholder_entry)
    )
}