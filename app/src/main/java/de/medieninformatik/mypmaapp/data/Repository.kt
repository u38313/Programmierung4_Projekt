package de.medieninformatik.mypmaapp.data

import de.medieninformatik.mypmaapp.data.db.ActivityLogEntity
import de.medieninformatik.mypmaapp.data.db.PmaDao
import de.medieninformatik.mypmaapp.data.db.PmaEntryEntity
import de.medieninformatik.mypmaapp.model.PmaEntry
import kotlinx.coroutines.flow.Flow

/* ─────────────────────────────────────────────────────────────
 * Repository: dünne Daten-Schicht über dem DAO
 * - Exponiert Flows für Entries & Logs
 * - Bietet suspend-Operationen für CRUD und Demo-Seeding
 * ───────────────────────────────────────────────────────────── */

/**
 * Zentraler Zugriffspunkt auf persistente Daten.
 * Umfasst Streams (Flows) für Änderungen sowie suspendierende
 * Schreib-/Lese-Operationen gegen die lokale DB.
 */
class Repository(private val dao: PmaDao) {

    /** Laufender Stream aller Einträge (für UI: StateFlow in VM). */
    val entries: Flow<List<PmaEntryEntity>> = dao.entriesFlow()

    /** Laufender Stream aller Aktivitäts-Logs. */
    val logs: Flow<List<ActivityLogEntity>> = dao.logsFlow()

    /**
     * Neuen Eintrag speichern.
     */
    suspend fun addEntry(
        title: String,
        desc: String,
        category: String,
        imageRes: Int
    ) {
        dao.insertEntry(
            PmaEntryEntity(
                title = title,
                description = desc,
                category = category,
                imageRes = imageRes
            )
        )
    }

    /**
     * Eintrag samt zugehöriger Logs löschen.
     * Reihenfolge wichtig: erst Logs, dann Entry (FK-Sauberkeit).
     */
    suspend fun deleteEntryById(id: Long) {
        dao.deleteLogsForEntry(id)   // verhindert verwaiste Logs
        dao.deleteEntryById(id)
    }

    /**
     * Aktivität für einen Eintrag protokollieren (Zeitstempel via Default in Entity).
     */
    suspend fun addLog(entryId: Long) {
        dao.insertLog(ActivityLogEntity(entryId = entryId))
    }

    /**
     * Demo-Daten idempotent einfügen.
     *
     * Es werden nur Einträge eingefügt, deren Titel noch nicht existieren.
     * Mapping Model → Entity übernimmt Felder 1:1 und bewahrt den Zeitstempel.
     */
    suspend fun seedDemoIfMissing(demo: List<PmaEntry>) {
        val existing = dao.getEntryTitles().toSet()
        val toInsert = demo
            .filter { it.title !in existing }      // einfache „Exists“-Heuristik per Titel
            .map {
                PmaEntryEntity(
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    imageRes = it.imageRes,
                    timestamp = it.timestamp
                )
            }

        if (toInsert.isNotEmpty()) {
            dao.insertEntries(toInsert)
        }
    }
}
