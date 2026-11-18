package uz.student.roomnotes.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

data class Address(
    val street: String,
    val city: String
)

// 10-savol: ikkinchi Address uchun prefix kerak, bo'lmasa ustun nomlari urishib ketadi
@Entity(tableName = "contact")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @Embedded
    val homeAddress: Address,
    @Embedded(prefix = "work_")
    val workAddress: Address
)
