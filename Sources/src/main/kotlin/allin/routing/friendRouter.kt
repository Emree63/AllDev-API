package allin.routing

import allin.dataSource
import allin.ext.hasToken
import allin.model.ApiMessage
import io.github.smiley4.ktorswaggerui.dsl.post
import io.ktor.http.*

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.friendRouter() {

    val userDataSource = this.dataSource.userDataSource
    val friendDataSource = this.dataSource.friendDataSource


    routing {
        authenticate {

        post("/friend/add", {
            description = "Allows a user to add a friend"
            request {
                headerParameter<JWTPrincipal>("JWT token of the logged user")
                body<String> {
                    description = "User to add in the friends list"
                }
            }
            response {
                HttpStatusCode.Created to {
                    description = "the friend has been added"
                    body<String>() {
                        description = "Friend with assigned id"
                    }
                }
                HttpStatusCode.Conflict to {
                    description = "Friend already exist in the friends list"
                    body(ApiMessage.FRIENDS_ALREADY_EXISTS)
                }
            }
        }) {
            hasToken { principal ->
                val requestMap = call.receive<Map<String, String>>()
                val usernameFriend = requestMap["username"] ?: return@hasToken call.respond(HttpStatusCode.BadRequest, "Username is missing")
                val username = tokenManagerBet.getUsernameFromToken(principal)

                val user = userDataSource.getUserByUsername(username).first
                val userFriend = userDataSource.getUserByUsername(usernameFriend).first

                if (user == null || userFriend == null) {
                    call.respond(HttpStatusCode.Conflict, ApiMessage.USER_NOT_FOUND)
                } else {
                    val friendlist = friendDataSource.getFriendFromUserId(user.id)
                    if (friendlist.contains(userFriend.id)) {
                        call.respond(HttpStatusCode.Conflict,ApiMessage.FRIENDS_ALREADY_EXISTS)
                    } else {
                        friendDataSource.addFriend(user.id, userFriend.id)
                        call.respond(HttpStatusCode.Created, usernameFriend)
                    }
                }
            }

        }
            post("/friend/delete", {
                description = "Allows a user to delete a friend"
                request {
                    headerParameter<JWTPrincipal>("JWT token of the logged user")
                    body<String> {
                        description = "User to delete in the friends list"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "the friend has been delete"
                        body<String>() {
                            description = "Friend with assigned id"
                        }
                    }
                    HttpStatusCode.Conflict to {
                        description = "Friend doesn't exist in the friends list"
                        body(ApiMessage.FRIENDS_DOESNT_EXISTS)
                    }
                }
            }) {
                hasToken { principal ->
                    val requestMap = call.receive<Map<String, String>>()
                    val usernameFriend = requestMap["username"] ?: return@hasToken call.respond(HttpStatusCode.BadRequest, "Username is missing")
                    val username = tokenManagerBet.getUsernameFromToken(principal)

                    val user = userDataSource.getUserByUsername(username).first
                    val userFriend = userDataSource.getUserByUsername(usernameFriend).first

                    if (user == null || userFriend == null) {
                        call.respond(HttpStatusCode.Conflict, ApiMessage.USER_NOT_FOUND)
                    } else {
                        val friendlist = friendDataSource.getFriendFromUserId(user.id)
                        if (!friendlist.contains(userFriend.id)) {
                            call.respond(HttpStatusCode.Conflict,ApiMessage.FRIENDS_DOESNT_EXISTS)
                        } else {
                            friendDataSource.deleteFriend(user.id, userFriend.id)
                            call.respond(HttpStatusCode.Created, usernameFriend)
                        }
                    }
                }

            }

        }
    }
}