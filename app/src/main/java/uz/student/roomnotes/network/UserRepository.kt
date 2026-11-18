package uz.student.roomnotes.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// offline-first: ekran har doim bazadan o'qiydi,
// refresh() internetdan olib bazani yangilaydi
class UserRepository(
    private val userDao: UserDao,
    private val service: GitHubService
) {

    fun users(): Flow<List<User>> {
        return userDao.observeAll().map { list ->
            list.map { User(it.login, it.id, it.avatarUrl) }
        }
    }

    suspend fun refresh() {
        val fromNetwork = service.users()
        val entities = fromNetwork.map { UserEntity(it.id, it.login, it.avatarUrl) }
        userDao.replaceAll(entities)
    }
}
