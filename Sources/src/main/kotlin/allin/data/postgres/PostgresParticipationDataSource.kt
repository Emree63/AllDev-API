package allin.data.postgres

import allin.data.ParticipationDataSource
import allin.data.postgres.entities.*
import allin.model.Participation
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insert
import org.ktorm.entity.*

class PostgresParticipationDataSource(private val database: Database) : ParticipationDataSource {

    override fun addParticipation(participation: Participation) {
        database.insert(ParticipationsEntity) {
            set(it.id, participation.id)
            set(it.betId, participation.betId)
            set(it.userid, participation.userId)
            set(it.answer, participation.answer)
            set(it.stake, participation.stake)
        }

        val betInfo = database.betInfos.find { it.id eq participation.betId } ?: BetInfoEntity {
            this.id = participation.betId
            this.totalStakes = 0
            this.totalParticipants = 0
        }

        betInfo.totalStakes += participation.stake
        betInfo.totalParticipants++
        database.betInfos.update(betInfo)

        database.betAnswerInfos.filter { it.betId eq participation.betId }.forEach {
            if (it.response == participation.answer) {
                it.totalStakes += participation.stake
            }

            val probability = (it.totalStakes / betInfo.totalStakes.toFloat())
            it.odds = if (probability == 0f) 1f else 1 / probability
            it.flushChanges()
        }
    }

    override fun getParticipationFromBetId(betid: String): List<Participation> =
        database.participations.filter { it.betId eq betid }.map { it.toParticipation(database) }

    override fun deleteParticipation(id: String): Boolean {
        val participation = database.participations.find { it.id eq id } ?: return false
        database.betInfos.find { it.id eq participation.bet.id }?.let { betInfo ->
            betInfo.totalStakes -= participation.stake
            betInfo.totalParticipants--

            database.betAnswerInfos.filter { it.betId eq participation.bet.id }.forEach {
                if (it.response == participation.answer) {
                    it.totalStakes -= participation.stake
                }
                val probability = it.totalStakes / betInfo.totalStakes.toFloat()
                it.odds = 1 / probability
                it.flushChanges()
            }

            betInfo.flushChanges()
        }
        return participation.delete() > 0
    }


}

