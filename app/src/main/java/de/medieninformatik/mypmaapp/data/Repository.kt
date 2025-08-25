package de.medieninformatik.mypmaapp.data

import de.medieninformatik.mypmaapp.data.db.*
import kotlinx.coroutines.flow.Flow

class Repository(private val dao: PmaDao) {
    val entries: Flow<List<PmaEntryEntity>> = dao.entriesFlow()
    val logs: Flow<List<ActivityLogEntity>> = dao.logsFlow()

    suspend fun addEntry(title: String, desc: String, category: String, imageRes: Int) {
        dao.insertEntry(PmaEntryEntity(title = title, description = desc, category = category, imageRes = imageRes))
    }
    suspend fun deleteEntryById(id: Long) {
        dao.deleteLogsForEntry(id)
        dao.deleteEntryById(id)
    }
    suspend fun addLog(entryId: Long) {
        dao.insertLog(ActivityLogEntity(entryId = entryId))
    }
}
