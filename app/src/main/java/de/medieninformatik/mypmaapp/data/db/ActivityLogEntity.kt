package de.medieninformatik.mypmaapp.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/* ─────────────────────────────────────────────────────────────
 * Activitäten Log Entity
 * Einzelner Log-Eintrag für eine aufgezeichnete Aktivität
 */
@Entity(
    tableName = "activity_logs",
    indices = [Index("entryId")]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long, //Primärschlüssel ID
    val timestamp: Long = System.currentTimeMillis()    //Zeitstempel
)
