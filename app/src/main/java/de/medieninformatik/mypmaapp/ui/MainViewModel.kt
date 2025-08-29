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



/** Model für ein einzelnes Aktivitäten-Log. */
data class ActivityLog(val id: Long, val entryId: Long, val timestamp: Long)

/* ----------------------------------------------------------------------------
 * ViewModel
 * ------------------------------------------------------------------------ */

/**
 * App-weites ViewModel.
 *
 * Verantwortlich für:
 * - Zugriff auf das Repository
 * - Exponieren von `entries` und `logs` als `StateFlow`
 * - Schreiboperationen (Einträge anlegen/löschen, Logs erfassen)
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    /** Zentrales Repository; bekommt den DAO aus der DB-Singleton. */
    private val repo = Repository(AppDatabase.get(app).dao())

    /**
     * Laufende Liste der Einträge (DB-Entity → UI-Modell gemappt).
     * `stateIn` teilt den Flow im ViewModel-Scope und liefert sofort einen Default-Wert.
     * Die WhileSubscribed-Strategie hält die Upstream-Sammlung noch ~5s aktiv, wenn keine Collector mehr da sind.
     */
    val entries: StateFlow<List<PmaEntry>> =
        repo.entries
            .map { list -> list.map { it.toModel() } } // Entity → Modell
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Laufende Liste der Aktivitäten-Logs (Entity → schlankes UI-Modell).
     */
    val logs: StateFlow<List<ActivityLog>> =
        repo.logs
            .map { list -> list.map { ActivityLog(it.id, it.entryId, it.timestamp) } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Legt einen neuen Eintrag an. */
    fun addEntry(title: String, desc: String, category: String, imageRes: Int) {
        viewModelScope.launch { repo.addEntry(title, desc, category, imageRes) }
    }

    /** Löscht einen Eintrag anhand der ID. */
    fun deleteEntry(id: Long) {
        viewModelScope.launch { repo.deleteEntryById(id) }
    }

    /** Fügt einen Log-Eintrag für einen bestehenden `entryId` hinzu. */
    fun addLog(entryId: Long) {
        viewModelScope.launch { repo.addLog(entryId) }
    }

    /**
     * Seedet Demo-Daten einmalig (nur fehlende Titel werden eingefügt).
     * Praktisch beim ersten App-Start.
     */
    fun seedDemoDefaults(demo: List<PmaEntry>) {
        viewModelScope.launch { repo.seedDemoIfMissing(demo) }
    }
}


/** Konvertiert eine persistierte `PmaEntryEntity` in das UI-Modell `PmaEntry`. */
private fun PmaEntryEntity.toModel() = PmaEntry(
    id = id,
    title = title,
    description = description,
    category = category,
    imageRes = imageRes,
    timestamp = timestamp
)
