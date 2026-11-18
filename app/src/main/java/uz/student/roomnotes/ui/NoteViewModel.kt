package uz.student.roomnotes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.student.roomnotes.data.AppDatabase
import uz.student.roomnotes.data.Note
import uz.student.roomnotes.data.NoteDao
import java.time.Instant

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {

    private val query = MutableStateFlow("")
    private val sortOrder = MutableStateFlow<SortOrder>(SortOrder.ByDate)

    val error = MutableStateFlow("")

    // 14-savol: har bosilgan harfga so'rov yubormaymiz, 300 ms kutamiz
    private val found = query
        .debounce(300)
        .flatMapLatest { text ->
            if (text.isBlank()) noteDao.observeAll() else noteDao.search(text)
        }

    val notes: StateFlow<List<NoteUi>> = combine(found, sortOrder) { list, order ->
        sortNotes(list, order).map { noteToUi(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun search(text: String) {
        query.value = text
    }

    fun changeSort(order: SortOrder) {
        sortOrder.value = order
    }

    // 12-savol: tekshirish ViewModel da bo'ladi
    fun add(title: String, body: String) {
        val clean = title.trim()
        if (clean.isEmpty()) {
            error.value = "Sarlavha bo'sh bo'lmasin"
            return
        }
        if (clean.length > 100) {
            error.value = "Sarlavha 100 belgidan uzun bo'lmasin"
            return
        }
        error.value = ""
        viewModelScope.launch {
            noteDao.insert(Note(title = clean, content = body, createdAt = Instant.now()))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            noteDao.deleteById(id)
        }
    }

    // 8-savol uchun: ataylab main thread da chaqirib ko'ramiz
    fun blockingCall(): String {
        return try {
            val list = noteDao.getAllBlocking()
            "ishladi, " + list.size + " ta eslatma"
        } catch (e: Exception) {
            e.javaClass.simpleName + ": " + e.message
        }
    }

    companion object {
        // 11-savol: DAO ni konstruktorga berish uchun factory
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                NoteViewModel(AppDatabase.get(app).noteDao())
            }
        }
    }
}
