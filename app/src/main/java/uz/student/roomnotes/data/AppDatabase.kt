package uz.student.roomnotes.data

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uz.student.roomnotes.network.UserDao
import uz.student.roomnotes.network.UserEntity
import java.util.concurrent.Executors

@Database(
    entities = [
        Note::class,
        Notebook::class,
        Tag::class,
        NoteTagCrossRef::class,
        Contact::class,
        UserEntity::class
    ],
    version = 4,
    autoMigrations = [AutoMigration(from = 2, to = 3, spec = RenameBodySpec::class)],
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun notebookDao(): NotebookDao
    abstract fun tagDao(): TagDao
    abstract fun contactDao(): ContactDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_3_4)
                    // 14-savol: Room qanday SQL yuborayotganini ko'rish uchun
                    .setQueryCallback(object : QueryCallback {
                        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                            Log.d("RoomSQL", sqlQuery + "  args=" + bindArgs)
                        }
                    }, Executors.newSingleThreadExecutor())
                    .build()
            }
            return instance!!
        }
    }
}
