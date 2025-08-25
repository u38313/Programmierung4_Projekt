package de.medieninformatik.mypmaapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.medieninformatik.mypmaapp.R
import de.medieninformatik.mypmaapp.data.Repository
import de.medieninformatik.mypmaapp.data.db.AppDatabase
import de.medieninformatik.mypmaapp.data.db.ActivityLogEntity
import de.medieninformatik.mypmaapp.data.db.PmaEntryEntity
import de.medieninformatik.mypmaapp.model.PmaEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActivityLog(val id: Long, val entryId: Long, val timestamp: Long)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app).dao())

    val entries: StateFlow<List<PmaEntry>> =
        repo.entries
            .map { list -> list.map { it.toModel() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val logs: StateFlow<List<ActivityLog>> =
        repo.logs
            .map { list -> list.map { ActivityLog(it.id, it.entryId, it.timestamp) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addEntry(title: String, desc: String, category: String, imageRes: Int) {
        viewModelScope.launch { repo.addEntry(title, desc, category, imageRes) }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { repo.deleteEntryById(id) }
    }

    fun addLog(entryId: Long) {
        viewModelScope.launch { repo.addLog(entryId) }
    }
}

/* ===== Mapping ===== */
private fun PmaEntryEntity.toModel() = PmaEntry(
    id = id,
    title = title,
    description = description,
    category = category,
    imageRes = imageRes,
    timestamp = timestamp
)
