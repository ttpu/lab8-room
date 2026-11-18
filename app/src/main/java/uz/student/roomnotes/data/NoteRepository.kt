package uz.student.roomnotes.data

import kotlinx.coroutines.flow.Flow

// 28-savol: ViewModel ni testda tekshirish uchun DAO ustiga yupqa qatlam
class NoteRepository(private val noteDao: NoteDao) {

    fun observeAll(): Flow<List<Note>> = noteDao.observeAll()

    fun search(q: String): Flow<List<Note>> = noteDao.search(q)

    suspend fun add(note: Note): Long = noteDao.insert(note)

    suspend fun deleteById(id: Long): Int = noteDao.deleteById(id)
}
