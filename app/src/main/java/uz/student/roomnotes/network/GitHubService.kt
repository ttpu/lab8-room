package uz.student.roomnotes.network

import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubService {

    @GET("/users")
    suspend fun users(): List<User>

    @GET("/users/{login}")
    suspend fun user(@Path("login") login: String): User
}
