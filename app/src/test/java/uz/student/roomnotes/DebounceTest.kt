package uz.student.roomnotes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uz.student.roomnotes.ui.NoteViewModel

// 14-savol: tez yozganda nechta so'rov ketadi
@OptIn(ExperimentalCoroutinesApi::class)
class DebounceTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun tezYozilgandaBittaSorovKetadi() = runTest {
        val dao = FakeNoteDao()
        val vm = NoteViewModel(dao)
        val job = launch { vm.notes.collect { } }
        advanceUntilIdle()
        dao.searchCalls = 0

        // "kotlin" so'zini tez yozamiz, har harf orasida 50 ms
        val sozlar = listOf("k", "ko", "kot", "kotl", "kotli", "kotlin")
        for (s in sozlar) {
            vm.search(s)
            advanceTimeBy(50)
        }
        advanceUntilIdle()

        println("tez yozganda qidiruv chaqirildi: " + dao.searchCalls + " marta")
        assertEquals(1, dao.searchCalls)
        job.cancel()
    }

    @Test
    fun sekinYozilgandaHarBiriUchunSorov() = runTest {
        val dao = FakeNoteDao()
        val vm = NoteViewModel(dao)
        val job = launch { vm.notes.collect { } }
        advanceUntilIdle()
        dao.searchCalls = 0

        // har harfdan keyin 400 ms kutamiz, debounce o'tib ketadi
        val sozlar = listOf("k", "ko", "kot")
        for (s in sozlar) {
            vm.search(s)
            advanceTimeBy(400)
        }
        advanceUntilIdle()

        println("sekin yozganda qidiruv chaqirildi: " + dao.searchCalls + " marta")
        assertEquals(3, dao.searchCalls)
        job.cancel()
    }
}
