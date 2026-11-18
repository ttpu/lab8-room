package uz.student.roomnotes.network

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

// 29-savol: GitHub userlarini telefonda saqlaymiz
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val login: String,
    val avatarUrl: String
)

@Dao
interface UserDao {

    @Query("SELECT * FROM user ORDER BY login")
    fun observeAll(): Flow<List<UserEntity>>

    @Insert
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM user")
    suspend fun clear()

    // eski ma'lumotni o'chirib, yangisini yozish - bitta tranzaksiyada
    @Transaction
    suspend fun replaceAll(users: List<UserEntity>) {
        clear()
        insertAll(users)
    }
}
