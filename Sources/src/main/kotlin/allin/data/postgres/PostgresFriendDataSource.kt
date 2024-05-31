package allin.data.postgres

import allin.data.FriendDataSource
import allin.data.postgres.entities.FriendEntity
import allin.data.postgres.entities.friends
import allin.data.postgres.entities.users
import allin.dto.UserDTO
import allin.ext.levenshtein
import allin.ext.toLowerCase
import allin.model.FriendStatus
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.notEq
import org.ktorm.entity.*

class PostgresFriendDataSource(private val database: Database) : FriendDataSource {
    override fun addFriend(sender: String, receiver: String) {
        database.friends.add(
            FriendEntity {
                this.sender = sender
                this.receiver = receiver
            }
        )
    }

    override fun getFriendFromUserId(id: String): List<UserDTO> {
        return database.friends.map { it.toFriend() }
            .filter { it.sender == id }
            .mapNotNull { friend ->
                val receiverUser = database.users.find { usr ->
                    usr.id eq friend.receiver
                }
                receiverUser?.toUserDTO(
                    database,
                    friendStatus = if (isFriend(friend.receiver, id)) {
                        FriendStatus.FRIEND
                    } else FriendStatus.REQUESTED
                )
            }
    }


    override fun getFriendRequestsFromUserId(id: String): List<UserDTO> {
        return database.friends
            .filter { it.receiver eq id }
            .mapNotNull {
                if (isFriend(firstUser = id, secondUser = it.sender)) {
                    null
                } else {
                    database.users.find { usr ->
                        usr.id eq it.sender
                    }?.toUserDTO(database,friendStatus = FriendStatus.NOT_FRIEND)
                }
            }
    }


    override fun deleteFriend(senderId: String, receiverId: String): Boolean {
        val result = database.friends.removeIf { (it.sender eq receiverId) and (it.receiver eq senderId) } +
                database.friends.removeIf { (it.sender eq senderId) and (it.receiver eq receiverId) }

        return result > 0
    }

    override fun isFriend(firstUser: String, secondUser: String) =
        database.friends.any { (it.sender eq firstUser) and (it.receiver eq secondUser) }

    override fun filterUsersByUsername(fromUserId: String, search: String): List<UserDTO> =
        database.users
            .filter { it.id notEq fromUserId }
            .sortedBy { it.username.toLowerCase().levenshtein(search.lowercase()) }
            .take(10)
            .map { user -> user.toUserDTO(database,friendStatus = getFriendStatus(fromUserId, user.id)) }
}