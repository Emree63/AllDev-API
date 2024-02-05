package allin.data.postgres

import allin.data.AllInDataSource
import allin.data.BetDataSource
import allin.data.ParticipationDataSource
import allin.data.UserDataSource
import allin.utils.Execute
import org.ktorm.database.Database

class PostgresDataSource : AllInDataSource() {

    private val database: Database

    init {
        val dbDatabase = System.getenv()["POSTGRES_DB"]
        val dbUser = System.getenv()["POSTGRES_USER"]
        val dbPassword = System.getenv()["POSTGRES_PASSWORD"]
        val dbHost = System.getenv()["POSTGRES_HOST"]

        database = Database.connect(
            url = "jdbc:postgresql://$dbHost/$dbDatabase",
            user = dbUser,
            password = dbPassword
        )

        database.Execute("CREATE TABLE IF not exists utilisateur ( id uuid PRIMARY KEY, username VARCHAR(255), password VARCHAR(255),coins double precision,email VARCHAR(255))")
        database.Execute("CREATE TABLE IF not exists bet ( id uuid PRIMARY KEY, theme VARCHAR(255), endregistration timestamp,endbet timestamp,sentencebet varchar(500),isprivate boolean, createdby varchar(250))")
        database.Execute("CREATE TABLE IF NOT EXISTS participation (id uuid PRIMARY KEY,bet uuid,username varchar(250),answer varchar(250),stake int);")
        database.Execute("CREATE TABLE IF NOT EXISTS response (id UUID,response VARCHAR(250),CONSTRAINT pk_response_id PRIMARY KEY (id,response));")
    }

    override val userDataSource: UserDataSource = PostgresUserDataSource(database)
    override val betDataSource: BetDataSource = PostgresBetDataSource(database)
    override val participationDataSource: ParticipationDataSource = PostgresParticipationDataSource(database)
}