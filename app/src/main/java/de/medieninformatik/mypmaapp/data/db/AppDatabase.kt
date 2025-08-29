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

/* -------------------------------------------------------------------
 * AppDatabase
 * Zentrale Room-Datenbank mit
 * Tabelle entries für Aktivitäteneinträge
 * Tabelle activity_logs für Aktivitäten Log Einträge
 */
@Database(
    entities = [PmaEntryEntity::class, ActivityLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Zugriffspunkt für Datenbankoperationen
    abstract fun dao(): PmaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Singleton-Instanz
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mypma.db"
                )
                    .addCallback(object : Callback() {

                        // Wird nur beim ersten Erstellen der DB aufgerufen
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            //Demo Daten Aktivitäten werden eingetragen
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
