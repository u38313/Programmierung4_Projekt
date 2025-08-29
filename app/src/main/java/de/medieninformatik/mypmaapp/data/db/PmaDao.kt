package de.medieninformatik.mypmaapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/* ─────────────────────────────────────────────────────────────
 * PmaDao
 * Datenzugriffsschicht (Room DAO) für Einträge (entries) und
 * Aktivitätsprotokolle (activity_logs).
 * - Streams liefern stets absteigend nach timestamp sortierte Listen.
 * - Für Löschvorgänge: Erst Logs zu einem Eintrag entfernen, dann den
 *   Eintrag selbst (sofern keine FK-CASCADEs aktiv sind).
 * ───────────────────────────────────────────────────────────── */
@Dao
interface PmaDao {

    /* ── Entries ───────────────────────────────────────────── */

    /** Kontinuierlicher Stream aller Einträge, neueste zuerst. */
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun entriesFlow(): Flow<List<PmaEntryEntity>>

    /** Einzelnen Eintrag einfügen; gibt die neue ID zurück. */
    @Insert
    suspend fun insertEntry(e: PmaEntryEntity): Long

    /** Eintrag per ID löschen. (Logs vorher entfernen, s. deleteLogsForEntry) */
    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    /** Mehrere Einträge in einem Rutsch einfügen (z. B. Seed-Daten). */
    @Insert
    suspend fun insertEntries(list: List<PmaEntryEntity>)

    /* ── Activity Logs ─────────────────────────────────────── */

    /** Kontinuierlicher Stream aller Aktivitätslogs, neueste zuerst. */
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun logsFlow(): Flow<List<ActivityLogEntity>>

    /** Einzelnes Aktivitätslog anlegen; gibt die neue ID zurück. */
    @Insert
    suspend fun insertLog(l: ActivityLogEntity): Long

    /** Alle Logs zu einem Eintrag löschen (vor deleteEntryById aufrufen). */
    @Query("DELETE FROM activity_logs WHERE entryId = :entryId")
    suspend fun deleteLogsForEntry(entryId: Long)

    /* ── Utilities ─────────────────────────────────────────── */

    /** Anzahl der vorhandenen Einträge. */
    @Query("SELECT COUNT(*) FROM entries")
    suspend fun countEntries(): Long

    /** Nur die Titel aller Einträge (z. B. zum Deduplizieren von Seed-Daten). */
    @Query("SELECT title FROM entries")
    suspend fun getEntryTitles(): List<String>
}
