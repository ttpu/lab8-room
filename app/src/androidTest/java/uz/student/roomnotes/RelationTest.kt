package uz.student.roomnotes

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uz.student.roomnotes.data.AppDatabase
import uz.student.roomnotes.data.Note
import uz.student.roomnotes.data.NoteTagCrossRef
import uz.student.roomnotes.data.Notebook
import uz.student.roomnotes.data.Tag
import java.time.Instant
import kotlin.test.assertFailsWith

// 18, 19, 20-savollar
class RelationTest {

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

    private fun eslatma(title: String, daftarId: Long?) =
        Note(title = title, content = "m", createdAt = Instant.now(), notebookId = daftarId)

    @Test
    fun daftarOchirilsaEslatmalarHamOchadi() = runBlocking {
        val daftarId = db.notebookDao().insert(Notebook(name = "Ish"))
        db.noteDao().insert(eslatma("Bir", daftarId))
        db.noteDao().insert(eslatma("Ikki", daftarId))
        assertEquals(2, db.noteDao().count())

        db.notebookDao().delete(Notebook(daftarId, "Ish"))

        // CASCADE ishlagan bo'lsa eslatmalar ham yo'q
        assertEquals(0, db.noteDao().count())
    }

    @Test
    fun yoqDaftargaEslatmaQoshilmaydi() = runBlocking {
        val xato = assertFailsWith<SQLiteConstraintException> {
            db.noteDao().insert(eslatma("Bir", 999))
        }
        println("xato: " + xato.message)
    }

    @Test
    fun replaceMalumotniYoqotadi() = runBlocking {
        val daftarId = db.notebookDao().insert(Notebook(name = "Ish"))
        db.noteDao().insertAll(
            listOf(eslatma("Bir", daftarId), eslatma("Ikki", daftarId), eslatma("Uch", daftarId))
        )
        assertEquals(3, db.noteDao().count())

        // REPLACE avval eski daftarni o'chiradi, cascade eslatmalarni olib ketadi
        db.notebookDao().insertOrReplace(Notebook(daftarId, "Ish (yangi nom)"))
        assertEquals(0, db.noteDao().count())
    }

    @Test
    fun upsertMalumotniSaqlaydi() = runBlocking {
        val daftarId = db.notebookDao().insert(Notebook(name = "Ish"))
        db.noteDao().insertAll(
            listOf(eslatma("Bir", daftarId), eslatma("Ikki", daftarId), eslatma("Uch", daftarId))
        )

        db.notebookDao().upsert(Notebook(daftarId, "Ish (yangi nom)"))

        assertEquals(3, db.noteDao().count())
    }

    @Test
    fun daftarVaEslatmalariBirgaKeladi() = runBlocking {
        val daftarId = db.notebookDao().insert(Notebook(name = "Uy"))
        db.noteDao().insert(eslatma("Bir", daftarId))
        db.noteDao().insert(eslatma("Ikki", daftarId))

        val list = db.notebookDao().observeWithNotes().first()
        assertEquals(1, list.size)
        assertEquals("Uy", list[0].notebook.name)
        assertEquals(2, list[0].notes.size)
    }

    @Test
    fun teglarIkkiTomondanOqiladi() = runBlocking {
        val noteId = db.noteDao().insert(eslatma("Bir", null))
        val noteId2 = db.noteDao().insert(eslatma("Ikki", null))
        val tagUy = db.tagDao().insertTag(Tag(name = "uy"))
        val tagIsh = db.tagDao().insertTag(Tag(name = "ish"))

        db.tagDao().link(NoteTagCrossRef(noteId, tagUy))
        db.tagDao().link(NoteTagCrossRef(noteId, tagIsh))
        db.tagDao().link(NoteTagCrossRef(noteId2, tagUy))

        val birinchi = db.tagDao().notePlusTags(noteId)!!
        assertEquals(2, birinchi.tags.size)

        val uyTegi = db.tagDao().tagPlusNotes(tagUy)!!
        assertEquals(2, uyTegi.notes.size)
    }
}
