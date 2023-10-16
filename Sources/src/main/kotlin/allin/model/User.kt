package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val username: String, val email: String, val password: String, var nbCoins: Int = 1000)

@Serializable
data class CheckUser(val login: String,val password: String)

fun isEmailValid(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
    return !emailRegex.matches(email)
}