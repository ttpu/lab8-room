package uz.student.roomnotes.data

import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// 24-savol: 1 -> 2 da priority ustuni qo'shiladi
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE note ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'")
    }
}

// 25-savol: 2 -> 3 da body ustuni content ga o'zgaradi.
// Buni Room o'zi qiladi, faqat nomini aytib qo'yish kerak.
@RenameColumn(tableName = "note", fromColumnName = "body", toColumnName = "content")
class RenameBodySpec : AutoMigrationSpec

// 25-savol: 3 -> 4 da is_archived ustunini olib tashlaymiz.
// SQLite da ustunni o'chirish yo'q, shuning uchun yangi jadval yasab,
// ma'lumotni ko'chirib, eskisini o'chiramiz.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `note_new` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, " +
                "`content` TEXT NOT NULL, " +
                "`created_at` INTEGER NOT NULL, " +
                "`pinned` INTEGER NOT NULL, " +
                "`priority` TEXT NOT NULL, " +
                "`notebookId` INTEGER, " +
                "`tags` TEXT NOT NULL, " +
                "FOREIGN KEY(`notebookId`) REFERENCES `notebook`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL(
            "INSERT INTO note_new (id, title, content, created_at, pinned, priority, notebookId, tags) " +
                "SELECT id, title, content, created_at, pinned, priority, notebookId, tags FROM note"
        )
        db.execSQL("DROP TABLE note")
        db.execSQL("ALTER TABLE note_new RENAME TO note")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_created_at` ON `note` (`created_at`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_note_notebookId` ON `note` (`notebookId`)")
    }
}
