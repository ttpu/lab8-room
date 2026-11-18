package uz.student.roomnotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notebook")
data class Notebook(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)
