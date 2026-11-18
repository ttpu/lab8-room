package uz.student.roomnotes

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uz.student.roomnotes.data.AppDatabase
import uz.student.roomnotes.data.MIGRATION_1_2
import uz.student.roomnotes.data.MIGRATION_3_4
import uz.student.roomnotes.data.RenameBodySpec

// 24, 25-savollar
class MigrationTest {

    private val testDb = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        listOf(RenameBodySpec())
    )

    @Test
    fun migratsiya1dan2ga() {
        var db = helper.createDatabase(testDb, 1)
        db.execSQL(
            "INSERT INTO note (title, body, created_at, pinned, is_archived, tags) " +
                "VALUES ('Bir', 'matn 1', 1000, 0, 0, '[]')"
        )
        db.execSQL(
            "INSERT INTO note (title, body, created_at, pinned, is_archived, tags) " +
                "VALUES ('Ikki', 'matn 2', 2000, 0, 0, '[]')"
        )
        db.close()

        db = helper.runMigrationsAndValidate(testDb, 2, true, MIGRATION_1_2)

        val cursor = db.query("SELECT title, priority FROM note ORDER BY created_at")
        assertEquals(2, cursor.count)
        cursor.moveToFirst()
        assertEquals("Bir", cursor.getString(0))
        assertEquals("NORMAL", cursor.getString(1))
        cursor.moveToNext()
        assertEquals("Ikki", cursor.getString(0))
        assertEquals("NORMAL", cursor.getString(1))
        cursor.close()
    }

    @Test
    fun migratsiya2dan3gaAvtomatik() {
        var db = helper.createDatabase(testDb, 2)
        db.execSQL(
            "INSERT INTO note (title, body, created_at, pinned, is_archived, priority, tags) " +
                "VALUES ('Bir', 'matn 1', 1000, 0, 0, 'HIGH', '[]')"
        )
        db.close()

        // body -> content ni Room o'zi o'zgartiradi
        db = helper.runMigrationsAndValidate(testDb, 3, true)

        val cursor = db.query("SELECT content FROM note")
        cursor.moveToFirst()
        assertEquals("matn 1", cursor.getString(0))
        cursor.close()
    }

    @Test
    fun migratsiya3dan4ga() {
        var db = helper.createDatabase(testDb, 3)
        db.execSQL(
            "INSERT INTO note (title, content, created_at, pinned, is_archived, priority, tags) " +
                "VALUES ('Bir', 'matn 1', 1000, 1, 1, 'LOW', '[\"uy\"]')"
        )
        db.close()

        db = helper.runMigrationsAndValidate(testDb, 4, true, MIGRATION_3_4)

        val cursor = db.query("SELECT title, content, priority, tags FROM note")
        assertEquals(1, cursor.count)
        cursor.moveToFirst()
        assertEquals("Bir", cursor.getString(0))
        assertEquals("matn 1", cursor.getString(1))
        assertEquals("LOW", cursor.getString(2))
        cursor.close()
    }
}
