package geofence

import java.sql.*

class SqliteDBConnect {
    private val currentDir = System.getProperty("user.dir")
    private val filename = "whereami-db"

    private val dbPath = "jdbc:sqlite:$currentDir/$filename"

    fun connection(): Connection {
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
        val connection = connection()
        val statement = connection.createStatement()
        statement.execute(sql)
        connection.close()
    }

    fun sqlExecuteQuery(sql: String):ResultSet {
        val connection = connection()
        val statement = connection.createStatement()
        val result = statement.executeQuery(sql)
        return result
    }
}