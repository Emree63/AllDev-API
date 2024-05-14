package allin.data.postgres

import allin.data.FriendDataSource
import allin.data.postgres.entities.FriendsEntity
import allin.model.User
import org.ktorm.database.Database
import org.ktorm.dsl.insert

class PostgresFriendDataSource(private val database: Database) : FriendDataSource {
    override fun addFriend(sender: String, receiver: String) {
        database.insert(FriendsEntity) {
            set(it.sender, sender)
            set(it.receiver, receiver)
        }
    }

    override fun getFriendFromUserId(id: String): List<User> {
        TODO()
    }

    override fun getFriendFromUsername(username: String) {
        TODO("Not yet implemented")
    }

    override fun deleteFriend(senderId: String, receiverId: String) {
        TODO("Not yet implemented")
    }

    override fun isFriend(firstUser: String, secondUser: String) {
        TODO("Not yet implemented")
    }

}