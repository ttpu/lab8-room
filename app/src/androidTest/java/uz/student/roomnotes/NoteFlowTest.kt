package uz.student.roomnotes

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uz.student.roomnotes.data.AppDatabase
import uz.student.roomnotes.data.Note
import uz.student.roomnotes.data.Notebook
import java.time.Instant

// 27-savol: Flow qayta chiqaradimi, Turbine bilan tekshiramiz
class NoteFlowTest {

    private lateinit var db: AppDatabase

    @Before
    fun ochish() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun yopish() {
        db.close()
    }

    @Test
    fun boshRoyxatKeyinQoshilgani() = runTest {
        db.noteDao().observeAll().test {
            assertEquals(0, awaitItem().size)
            db.noteDao().insert(Note(title = "Salom", content = "m", createdAt = Instant.now()))
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun yangilashYangiRoyxatChiqaradi() = runTest {
        val id = db.noteDao().insert(Note(title = "Eski", content = "m", createdAt = Instant.now()))
        db.noteDao().observeAll().test {
            assertEquals("Eski", awaitItem()[0].title)
            val note = db.noteDao().getById(id)!!
            db.noteDao().update(note.copy(title = "Yangi"))
            assertEquals("Yangi", awaitItem()[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun boshqaJadvalgaYozishTegmaydi() = runTest {
        db.noteDao().observeAll().test {
            awaitItem()
            db.notebookDao().insert(Notebook(name = "Ish"))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
