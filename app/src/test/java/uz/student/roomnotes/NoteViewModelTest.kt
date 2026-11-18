package uz.student.roomnotes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uz.student.roomnotes.ui.NoteViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun boshSarlavhaQabulQilinmaydi() = runTest {
        val dao = FakeNoteDao()
        val vm = NoteViewModel(dao)

        vm.add("   ", "matn")
        advanceUntilIdle()

        assertEquals(0, dao.getAll().size)
        assertEquals("Sarlavha bo'sh bo'lmasin", vm.error.value)
    }

    @Test
    fun sarlavhaTrimQilinadi() = runTest {
        val dao = FakeNoteDao()
        val vm = NoteViewModel(dao)

        vm.add("  Salom  ", "matn")
        advanceUntilIdle()

        assertEquals("Salom", dao.getAll()[0].title)
    }

    @Test
    fun uzunSarlavhaQabulQilinmaydi() = runTest {
        val dao = FakeNoteDao()
        val vm = NoteViewModel(dao)

        val uzun = "a".repeat(101)
        vm.add(uzun, "matn")
        advanceUntilIdle()

        assertEquals(0, dao.getAll().size)
        assertEquals("Sarlavha 100 belgidan uzun bo'lmasin", vm.error.value)
    }

    @Test
    fun yuzBelgiliSarlavhaOtadi() = runTest {
        val dao = FakeNoteDao()
        val vm = NoteViewModel(dao)

        vm.add("b".repeat(100), "matn")
        advanceUntilIdle()

        assertEquals(1, dao.getAll().size)
    }

    @Test
    fun qoshilganEslatmaRoyxatgaTushadi() = runTest {
        val dao = FakeNoteDao()
        val vm = NoteViewModel(dao)
        // stateIn WhileSubscribed bo'lgani uchun kimdir o'qib turishi kerak
        val job = launch { vm.notes.collect { } }

        vm.add("Salom", "birinchi eslatma")
        advanceUntilIdle()

        assertEquals(1, vm.notes.value.size)
        assertEquals("Salom", vm.notes.value[0].title)
        job.cancel()
    }

    @Test
    fun ochirishIshlaydi() = runTest {
        val dao = FakeNoteDao()
        val vm = NoteViewModel(dao)
        val job = launch { vm.notes.collect { } }

        vm.add("Salom", "matn")
        advanceUntilIdle()
        val id = dao.getAll()[0].id

        vm.delete(id)
        advanceUntilIdle()

        assertEquals(0, dao.getAll().size)
        job.cancel()
    }
}
