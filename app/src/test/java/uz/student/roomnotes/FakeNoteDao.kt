package uz.student.roomnotes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import uz.student.roomnotes.data.Note
import uz.student.roomnotes.data.NoteDao

// 28-savol: baza va emulator kerak bo'lmasligi uchun yasama DAO
class FakeNoteDao : NoteDao {

    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1L

    // 14-savol: qidiruv necha marta chaqirildi
    var searchCalls = 0

    override suspend fun insert(note: Note): Long {
        val yangi = note.copy(id = nextId)
        nextId++
        notes.value = notes.value + yangi
        return yangi.id
    }

    override suspend fun insertAll(notes2: List<Note>): List<Long> {
        val idlar = mutableListOf<Long>()
        for (n in notes2) {
            idlar.add(insert(n))
        }
        return idlar
    }

    override suspend fun update(note: Note): Int {
        val list = notes.value.toMutableList()
        for (i in list.indices) {
            if (list[i].id == note.id) {
                list[i] = note
                notes.value = list
                return 1
            }
        }
        return 0
    }

    override suspend fun delete(note: Note): Int {
        return deleteById(note.id)
    }

    override suspend fun deleteById(id: Long): Int {
        val oldSize = notes.value.size
        notes.value = notes.value.filter { it.id != id }
        return oldSize - notes.value.size
    }

    override suspend fun upsert(note: Note) {
        if (note.id == 0L || getById(note.id) == null) {
            insert(note)
        } else {
            update(note)
        }
    }

    override fun observeAll(): Flow<List<Note>> = notes

    override suspend fun getAll(): List<Note> = notes.value

    override fun getAllBlocking(): List<Note> = notes.value

    override suspend fun getById(id: Long): Note? = notes.value.firstOrNull { it.id == id }

    override fun search(q: String): Flow<List<Note>> {
        searchCalls++
        return notes.map { list -> list.filter { it.title.contains(q, true) } }
    }

    override suspend fun count(): Int = notes.value.size

    override fun observeCount(): Flow<Int> = notes.map { it.size }

    override suspend fun createdAfter(time: Long): List<Note> {
        return notes.value.filter { it.createdAt.toEpochMilli() > time }
    }
}
