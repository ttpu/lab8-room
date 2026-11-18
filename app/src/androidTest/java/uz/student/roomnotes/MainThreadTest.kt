package uz.student.roomnotes

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.student.roomnotes.data.AppDatabase

// 8-savol: main thread da bloklovchi so'rov nima bo'ladi
class MainThreadTest {

    @Test
    fun mainThreaddaXatoBeradi() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val db = Room.inMemoryDatabaseBuilder(
            instrumentation.targetContext,
            AppDatabase::class.java
        ).build()

        var xato = ""
        instrumentation.runOnMainSync {
            try {
                db.noteDao().getAllBlocking()
                xato = "xato chiqmadi"
            } catch (e: Exception) {
                xato = e.javaClass.name + ": " + e.message
            }
        }
        println("main thread: " + xato)
        db.close()

        assertTrue(xato.contains("IllegalStateException"))
    }

    @Test
    fun allowMainThreadQueriesBilanIshlaydi() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val db = Room.inMemoryDatabaseBuilder(
            instrumentation.targetContext,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        var natija = ""
        instrumentation.runOnMainSync {
            val list = db.noteDao().getAllBlocking()
            natija = "ishladi, " + list.size + " ta"
        }
        println("allowMainThreadQueries: " + natija)
        db.close()

        assertTrue(natija.startsWith("ishladi"))
    }
}
