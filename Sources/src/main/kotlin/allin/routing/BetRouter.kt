package allin.routing

import allin.ext.hasToken
import allin.ext.verifyUserFromToken
import allin.model.ApiMessage
import allin.model.Bet
import allin.model.BetWithoutId
import allin.model.UpdatedBetData
import allin.utils.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

val bets = mutableListOf<Bet>()
val tokenManagerBet = AppConfig.tokenManager

fun Application.BetRouter() {
    routing {
        route("/bets/add") {
            post {
                val bet = call.receive<BetWithoutId>()
                val id = UUID.randomUUID().toString()
                val username = tokenManagerBet.getUsernameFromToken(bet.createdBy)
                bets.find { it.id == id }?.let {
                    call.respond(HttpStatusCode.Conflict, ApiMessage.BetAlreadyExist)
                } ?: run {
                    val betWithId = Bet(
                        id,
                        bet.theme,
                        bet.sentenceBet,
                        bet.endRegistration,
                        bet.endBet,
                        bet.isPrivate,
                        bet.response,
                        username
                    )
                    bets.add(betWithId)
                    call.respond(HttpStatusCode.Created, betWithId)
                }
            }
        }
        route("/bets/gets") {
            get {
                // if(bets.size>0)
                call.respond(HttpStatusCode.Accepted, bets.toList())
                // else call.respond(HttpStatusCode.NoContent)
            }
        }
        route("/bets/delete") {
            post {
                val idbet = call.receive<Map<String, String>>()["id"]
                bets.find { it.id == idbet }?.let { findbet ->
                    bets.remove(findbet)
                    call.respond(HttpStatusCode.Accepted, findbet)
                } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.BetNotFound)
            }
        }
        route("bets/update") {
            post {
                val updatedBetData = call.receive<UpdatedBetData>()
                bets.find { it.id == updatedBetData.id }?.let { findbet ->
                    findbet.endBet = updatedBetData.endBet
                    findbet.isPrivate = updatedBetData.isPrivate
                    findbet.response = updatedBetData.response
                    call.respond(HttpStatusCode.Accepted, findbet)
                } ?: call.respond(HttpStatusCode.NotFound, ApiMessage.BetNotFound)
            }
        }

        authenticate {
            get("/bets/current") {
                hasToken { principal ->
                    verifyUserFromToken(principal) { user ->
                        val bets = participations
                            .filter { it.username == user.username }
                            .mapNotNull { itParticipation -> bets.find { it.id == itParticipation.betId } }
                        call.respond(HttpStatusCode.OK, bets)
                    }
                }
            }
        }
    }
}