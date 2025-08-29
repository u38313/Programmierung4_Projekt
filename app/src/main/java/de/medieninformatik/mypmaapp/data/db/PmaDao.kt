package de.medieninformatik.mypmaapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/* --------------------------------------------------------------------
 * Zugriffsqueries auf Einträge der Datenbank
 */
@Dao
interface PmaDao {

    /* ---------------------------------------------------
    Aktivitäteneinträge
    */

    // Kontinuierlicher Stream aller Einträge
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun entriesFlow(): Flow<List<PmaEntryEntity>>

    // Einzelnen Eintrag einfügen
    @Insert
    suspend fun insertEntry(e: PmaEntryEntity): Long

    // Eintrag per ID löschen
    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    // Mehrere Einträge einfügen
    @Insert
    suspend fun insertEntries(list: List<PmaEntryEntity>)

    /* --------------------------------------------------------------------------
    Activitäten Log Einträge
    */

    // Kontinuierlicher Stream aller Aktivitätslogs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun logsFlow(): Flow<List<ActivityLogEntity>>

    // Einzelnes Aktivitätslog einfügen
    @Insert
    suspend fun insertLog(l: ActivityLogEntity): Long

    // Alle Logs zu einem Aktivitätseintrag löschen
    @Query("DELETE FROM activity_logs WHERE entryId = :entryId")
    suspend fun deleteLogsForEntry(entryId: Long)

    /* -----------------------------------------------------------------------------
     Utilities
     */

    // Anzahl der vorhandenen Einträge
    @Query("SELECT COUNT(*) FROM entries")
    suspend fun countEntries(): Long

    // Titel aller Einträge
    @Query("SELECT title FROM entries")
    suspend fun getEntryTitles(): List<String>
}
