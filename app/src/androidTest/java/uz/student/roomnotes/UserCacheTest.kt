package uz.student.roomnotes

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uz.student.roomnotes.data.AppDatabase
import uz.student.roomnotes.network.ApiInstance
import uz.student.roomnotes.network.UserRepository

// 29-savol: offline-first
class UserCacheTest {

    private lateinit var db: AppDatabase
    private lateinit var server: MockWebServer

    private val javob = """
        [
          {"login":"mojombo","id":1,"avatar_url":"https://a/1"},
          {"login":"defunkt","id":2,"avatar_url":"https://a/2"}
        ]
    """.trimIndent()

    @Before
    fun ochish() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        server = MockWebServer()
        server.start()
    }

    @After
    fun yopish() {
        db.close()
        try {
            server.shutdown()
        } catch (e: Exception) {
            // test ichida allaqachon yopilgan bo'lishi mumkin
        }
    }

    private fun repo(): UserRepository {
        val service = ApiInstance.createService(server.url("/").toString())
        return UserRepository(db.userDao(), service)
    }

    @Test
    fun birinchiMartaInternetdanOlibSaqlaydi() = runBlocking {
        server.enqueue(MockResponse().setBody(javob))
        val repository = repo()

        repository.refresh()

        val list = repository.users().first()
        assertEquals(2, list.size)
        // login bo'yicha tartiblangan
        assertEquals("defunkt", list[0].login)
    }

    @Test
    fun internetYoqdaKeshKorinadi() = runBlocking {
        server.enqueue(MockResponse().setBody(javob))
        val repository = repo()
        repository.refresh()

        server.shutdown()
        try {
            repository.refresh()
        } catch (e: Exception) {
            println("internet yo'q: " + e.javaClass.simpleName)
        }

        assertEquals(2, repository.users().first().size)
    }

    @Test
    fun xato404daKeshOzgarmaydi() = runBlocking {
        server.enqueue(MockResponse().setBody(javob))
        val repository = repo()
        repository.refresh()

        server.enqueue(MockResponse().setResponseCode(404))
        try {
            repository.refresh()
        } catch (e: Exception) {
            println("404: " + e.javaClass.simpleName)
        }

        assertEquals(2, repository.users().first().size)
    }
}
