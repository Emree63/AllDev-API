package allin.data.postgres.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface UserEntity : Entity<UserEntity> {
    val username: String
    var email: String
    var password: String
    var nbCoins: Int
}

object UsersEntity : Table<UserEntity>("utilisateur") {
    val id = uuid("id").primaryKey()
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
    val nbCoins = int("coins").bindTo { it.nbCoins }
    val email = varchar("email").bindTo { it.email }
    val lastGift = timestamp("lastgift")
}
