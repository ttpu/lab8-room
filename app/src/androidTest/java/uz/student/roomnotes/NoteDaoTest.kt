package uz.student.roomnotes

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uz.student.roomnotes.data.AppDatabase
import uz.student.roomnotes.data.Note
import uz.student.roomnotes.data.NoteDao
import uz.student.roomnotes.data.Priority
import java.time.Instant

// 26-savol: baza xotirada, har test uchun yangisi
class NoteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: NoteDao

    @Before
    fun ochish() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.noteDao()
    }

    @After
    fun yopish() {
        db.close()
    }

    private fun eslatma(title: String, vaqt: Long) =
        Note(title = title, content = "matn", createdAt = Instant.ofEpochMilli(vaqt))

    @Test
    fun qoshibOqish() = runBlocking {
        val id = dao.insert(eslatma("Birinchi", 1000))
        val olingan = dao.getById(id)
        assertEquals("Birinchi", olingan!!.title)
    }

    @Test
    fun yangilash() = runBlocking {
        val id = dao.insert(eslatma("Eski", 1000))
        val note = dao.getById(id)!!
        val nechta = dao.update(note.copy(title = "Yangi"))
        assertEquals(1, nechta)
        assertEquals("Yangi", dao.getById(id)!!.title)
    }

    @Test
    fun ochirish() = runBlocking {
        val id = dao.insert(eslatma("Birinchi", 1000))
        val nechta = dao.deleteById(id)
        assertEquals(1, nechta)
        assertNull(dao.getById(id))
    }

    @Test
    fun yoqQatorniYangilaganda() = runBlocking {
        // 4-savol: bo'lmagan qator uchun 0 qaytadi
        val nechta = dao.update(Note(id = 999, title = "Yo'q", content = "m", createdAt = Instant.now()))
        assertEquals(0, nechta)
        assertEquals(0, dao.deleteById(999))
    }

    @Test
    fun tartibYangisiTepada() = runBlocking {
        dao.insert(eslatma("Eski", 1000))
        dao.insert(eslatma("Yangi", 5000))
        val list = dao.getAll()
        assertEquals("Yangi", list[0].title)
        assertEquals("Eski", list[1].title)
    }

    @Test
    fun qidirish() = runBlocking {
        dao.insert(eslatma("Salom dunyo", 1000))
        dao.insert(eslatma("Xayr", 2000))
        dao.insert(eslatma("Привет", 3000))

        val topildi = dao.search("salom").first()
        assertEquals(1, topildi.size)
        assertEquals("Salom dunyo", topildi[0].title)
        // 6-savol: lotin harfida LIKE registrga qaramaydi
        assertEquals(1, dao.createdAfter(2500).size)
    }

    @Test
    fun upsertYangisiniQoshadi() = runBlocking {
        dao.upsert(eslatma("Birinchi", 1000))
        assertEquals(1, dao.count())
        val note = dao.getAll()[0]
        dao.upsert(note.copy(title = "O'zgardi", priority = Priority.HIGH))
        assertEquals(1, dao.count())
        assertEquals("O'zgardi", dao.getAll()[0].title)
    }

    @Test
    fun teglarVaPriorityRoundTrip() = runBlocking {
        // 9-savol: converter ishlayaptimi
        val id = dao.insert(
            Note(
                title = "Teg",
                content = "m",
                createdAt = Instant.ofEpochMilli(1000),
                priority = Priority.HIGH,
                tags = listOf("uy, ish", "\"qo'shtirnoq\"", "")
            )
        )
        val olingan = dao.getById(id)!!
        assertEquals(listOf("uy, ish", "\"qo'shtirnoq\"", ""), olingan.tags)
        assertEquals(Priority.HIGH, olingan.priority)
    }
}
