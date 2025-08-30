package de.medieninformatik.mypmaapp.data

import de.medieninformatik.mypmaapp.data.db.ActivityLogEntity
import de.medieninformatik.mypmaapp.data.db.PmaDao
import de.medieninformatik.mypmaapp.data.db.PmaEntryEntity
import de.medieninformatik.mypmaapp.model.PmaEntry
import kotlinx.coroutines.flow.Flow


/*
 * Zugriffspunkt auf Daten der DB
 */
class Repository(private val dao: PmaDao) {

    // Laufender Stream aller Einträge
    val entries: Flow<List<PmaEntryEntity>> = dao.entriesFlow()

    // Laufender Stream aller Aktivitäts-Logs
    val logs: Flow<List<ActivityLogEntity>> = dao.logsFlow()

    //Neuen Aktivitätseintrag speichern
    suspend fun addEntry(title: String, desc: String, category: String, imageRes: Int
    ) {
        dao.insertEntry(
            PmaEntryEntity(title = title, description = desc, category = category, imageRes = imageRes
            )
        )
    }

    //Aktivitätseintrag löschen
    suspend fun deleteEntryById(id: Long) {
        dao.deleteLogsForEntry(id)   //löscht auch Aktivitätslogs
        dao.deleteEntryById(id)
    }

    // Aktivitätslog eines Aktivitätseintrag speichern
    suspend fun addLog(entryId: Long) {
        dao.insertLog(ActivityLogEntity(entryId = entryId))
    }
}
