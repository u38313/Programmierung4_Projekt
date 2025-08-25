package de.medieninformatik.mypmaapp.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PmaDao {
    // Entries
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun entriesFlow(): Flow<List<PmaEntryEntity>>

    @Insert
    suspend fun insertEntry(e: PmaEntryEntity): Long

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Insert
    suspend fun insertEntries(list: List<PmaEntryEntity>)

    // Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun logsFlow(): Flow<List<ActivityLogEntity>>

    @Insert
    suspend fun insertLog(l: ActivityLogEntity): Long

    @Query("DELETE FROM activity_logs WHERE entryId = :entryId")
    suspend fun deleteLogsForEntry(entryId: Long)
}
