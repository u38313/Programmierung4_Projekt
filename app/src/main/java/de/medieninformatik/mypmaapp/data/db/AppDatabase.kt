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

@Database(
    entities = [PmaEntryEntity::class, ActivityLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): PmaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mypma.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Preseed mit DemoData (nur beim allerersten Erstellen)
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
                    .build().also { INSTANCE = it }
            }
    }
}
