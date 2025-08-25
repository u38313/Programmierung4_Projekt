package de.medieninformatik.mypmaapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class PmaEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val imageRes: Int,
    val timestamp: Long = System.currentTimeMillis()
)