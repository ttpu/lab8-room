package uz.student.roomnotes

import org.junit.Assert.assertEquals
import org.junit.Test
import uz.student.roomnotes.data.Note
import uz.student.roomnotes.ui.SortOrder
import uz.student.roomnotes.ui.sortNotes
import java.time.Instant

class SortOrderTest {

    // 15-savol: beshta yasama eslatma, ikkitasining nomi bir xil
    private val list = listOf(
        Note(1, "Banan", "x", Instant.ofEpochMilli(1000)),
        Note(2, "Alma", "x", Instant.ofEpochMilli(2000)),
        Note(3, "Alma", "x", Instant.ofEpochMilli(3000)),
        Note(4, "Shaftoli", "x", Instant.ofEpochMilli(4000), pinned = true),
        Note(5, "Olma", "x", Instant.ofEpochMilli(5000))
    )

    @Test
    fun sanaBoyichaYangisiTepada() {
        val natija = sortNotes(list, SortOrder.ByDate)
        assertEquals(listOf(5L, 4L, 3L, 2L, 1L), natija.map { it.id })
    }

    @Test
    fun nomBoyicha() {
        val natija = sortNotes(list, SortOrder.ByTitle)
        assertEquals(
            listOf("Alma", "Alma", "Banan", "Olma", "Shaftoli"),
            natija.map { it.title }
        )
    }

    @Test
    fun pinnedBirinchi() {
        val natija = sortNotes(list, SortOrder.PinnedFirst)
        assertEquals(4L, natija[0].id)
        assertEquals(5L, natija[1].id)
    }
}
