package allin.ext

import allin.dto.UserDTO
import allin.entities.UsersEntity
import allin.model.ApiMessage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.hasToken(content: suspend (principal: JWTPrincipal) -> Unit) =
    call.principal<JWTPrincipal>()?.let { content(it) } ?: call.respond(HttpStatusCode.Unauthorized)

suspend fun PipelineContext<*, ApplicationCall>.verifyUserFromToken(
    principal: JWTPrincipal,
    content: suspend (user: UserDTO, password: String) -> Unit
) {
    val username = principal.payload.getClaim("username").asString()
    val userPassword = UsersEntity.getUserByUsernameAndPassword(username)
    userPassword.first?.let { content(it, userPassword.second ?: "") }
        ?: call.respond(HttpStatusCode.NotFound, ApiMessage.TokenUserNotFound)
}