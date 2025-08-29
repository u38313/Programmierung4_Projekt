package de.medieninformatik.mypmaapp.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/* ─────────────────────────────────────────────────────────────
 * ActivityLogEntity
 * Einzelner Log-Eintrag für eine aufgezeichnete Aktivität.
 *
 * - Tabelle: "activity_logs"
 * - Index auf entryId für schnelle Lookups pro Eintrag
 * - Felder:
 *     • id         – Primärschlüssel (auto-increment)
 *     • entryId    – verweist logisch auf PmaEntryEntity.id
 *     • timestamp  – Zeitpunkt der Aufzeichnung (EpochMillis, now() Default)
 * ───────────────────────────────────────────────────────────── */
@Entity(
    tableName = "activity_logs",
    indices = [Index("entryId")]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
