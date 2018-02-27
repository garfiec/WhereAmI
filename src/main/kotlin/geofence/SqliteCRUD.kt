package geofence

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class SqliteCRUD {
    private val currentDir = System.getProperty("user.dir")
    private val filename = "whereami-db"

    private val dbPath = "jdbc:sqlite:$currentDir/$filename"

    private fun connection(): Connection {
        try {
            return DriverManager.getConnection(dbPath)
        } catch (ex: SQLException) {
            println("Error: " + ex.message)
            System.exit(0)
        }
        TODO("May need a better solution here for kotlin's null safety")
        return DriverManager.getConnection("")
    }

    // Connects to database file. Create if not exist
    fun connectDatabase() {
        val connection = connection()
        val meta = connection.metaData
        println("Using JDBC drivers: $meta")
    }


    fun sqlExecute(sql: String) {
        val statement = connection().createStatement()
        statement.execute(sql)
    }

    fun sqlExecuteQuery(sql: String):ResultSet {
        val statement = connection().createStatement()

        return statement.executeQuery(sql)
    }

}