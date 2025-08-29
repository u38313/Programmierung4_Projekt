package de.medieninformatik.mypmaapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.medieninformatik.mypmaapp.data.Repository
import de.medieninformatik.mypmaapp.data.db.AppDatabase
import de.medieninformatik.mypmaapp.data.db.PmaEntryEntity
import de.medieninformatik.mypmaapp.model.PmaEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



/* Model für ein einzelnes Aktivitäten-Log. */
data class ActivityLog(val id: Long, val entryId: Long, val timestamp: Long)

/* ----------------------------------------------------------------------------
 * ViewModel mit
 * Zugriff auf Repository
 * Anlegen, Löschen und Erfassen von Einträgen und Logs
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    // Repository bekommt den DAO aus der DB
    private val repo = Repository(AppDatabase.get(app).dao())


    // Laufende Liste der Einträge
    val entries: StateFlow<List<PmaEntry>> =
        repo.entries
            .map { list -> list.map { it.toModel() } } // Entity → Modell
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    //Laufende Liste der Aktivitäten-Logs
    val logs: StateFlow<List<ActivityLog>> =
        repo.logs
            .map { list -> list.map { ActivityLog(it.id, it.entryId, it.timestamp) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Legt einen neuen Eintrag an
    fun addEntry(title: String, desc: String, category: String, imageRes: Int) {
        viewModelScope.launch { repo.addEntry(title, desc, category, imageRes) }
    }

    // Löscht einen Eintrag anhand der ID
    fun deleteEntry(id: Long) {
        viewModelScope.launch { repo.deleteEntryById(id) }
    }

    // Fügt einen Log-Eintrag für einen bestehenden `entryId` hinzu
    fun addLog(entryId: Long) {
        viewModelScope.launch { repo.addLog(entryId) }
    }
}


// Konvertiert DB-Eintrag in UI-Model-Eintrag
private fun PmaEntryEntity.toModel() = PmaEntry(
    id = id,
    title = title,
    description = description,
    category = category,
    imageRes = imageRes,
    timestamp = timestamp
)
