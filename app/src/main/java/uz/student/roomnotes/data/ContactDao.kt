package uz.student.roomnotes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContactDao {

    @Insert
    suspend fun insert(contact: Contact): Long

    @Query("SELECT * FROM contact")
    suspend fun getAll(): List<Contact>
}
