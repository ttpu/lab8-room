package uz.student.roomnotes

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import uz.student.roomnotes.data.AppDatabase
import uz.student.roomnotes.data.Note
import java.time.Instant

// 21-savol: indeks bormi-yo'qmi, so'rov rejasi o'zgaradimi
class IndexTest {

    private lateinit var db: AppDatabase

    @Before
    fun ochish() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun yopish() {
        db.close()
    }

    private fun rejaniChiqar(sarlavha: String) {
        val cursor = db.query("EXPLAIN QUERY PLAN SELECT * FROM note WHERE created_at > ?", arrayOf(25000L))
        while (cursor.moveToNext()) {
            // oxirgi ustunda rejaning matni turadi
            println(sarlavha + " -> " + cursor.getString(cursor.columnCount - 1))
        }
        cursor.close()
    }

    private fun vaqtniOlchash(): Long {
        val boshlandi = System.currentTimeMillis()
        val cursor = db.query("SELECT * FROM note WHERE created_at > ?", arrayOf(49000L))
        var nechta = 0
        while (cursor.moveToNext()) {
            nechta++
        }
        cursor.close()
        val vaqt = System.currentTimeMillis() - boshlandi
        println("topildi " + nechta + " ta, vaqt " + vaqt + " ms")
        return vaqt
    }

    @Test
    fun indeksBilanVaIndeksdanKeyin() = runBlocking {
        // 50 000 eslatma yozamiz
        val list = mutableListOf<Note>()
        for (i in 1..50000) {
            list.add(
                Note(
                    title = "Eslatma " + i,
                    content = "matn " + i,
                    createdAt = Instant.ofEpochMilli(i.toLong())
                )
            )
        }
        db.noteDao().insertAll(list)
        println("jami: " + db.noteDao().count())

        rejaniChiqar("indeks bor")
        val vaqtIndeksBilan = vaqtniOlchash()

        // indeksni o'chirib yuboramiz va yana qaraymiz
        db.openHelper.writableDatabase.execSQL("DROP INDEX index_note_created_at")

        rejaniChiqar("indeks yo'q")
        val vaqtIndeksdanKeyin = vaqtniOlchash()

        println("indeks bilan " + vaqtIndeksBilan + " ms, indeksdan keyin " + vaqtIndeksdanKeyin + " ms")
    }
}
