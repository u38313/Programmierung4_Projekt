package de.medieninformatik.mypmaapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.medieninformatik.mypmaapp.data.DemoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/* ─────────────────────────────────────────────────────────────
 * AppDatabase
 * Zentrale Room-Datenbank.
 *
 * - Beinhaltet die Tabellen:
 *     • entries (PmaEntryEntity)
 *     • activity_logs (ActivityLogEntity)
 * - Stellt das DAO (PmaDao) bereit.
 * - Erstinitialisierung: Preseed mit DemoData in onCreate().
 *   (wird nur beim allerersten Anlegen der DB ausgeführt)
 * ───────────────────────────────────────────────────────────── */
@Database(
    entities = [PmaEntryEntity::class, ActivityLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Zugriffspunkt für alle Datenbankoperationen. */
    abstract fun dao(): PmaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /** Singleton-Instanz der Datenbank liefern/erstellen. */
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mypma.db"
                )
                    .addCallback(object : Callback() {

                        /** Wird nur beim ersten Erstellen der DB aufgerufen. */
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            // Preseed: Demo-Einträge asynchron einfügen.
                            // Hinweis: Wir holen uns ein DAO über get(context),
                            // um Inserts bequem per DAO vorzunehmen.
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = get(context).dao()
                                val initial = DemoData.initial().map {
                                    PmaEntryEntity(
                                        title = it.title,
                                        description = it.description,
                                        category = it.category,
                                        imageRes = it.imageRes,
                                        timestamp = it.timestamp
                                    )
                                }
                                dao.insertEntries(initial)
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
