package geofence

import network.NetworkRecorder
import network.NetworkScanner
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

class SqliteDataAccess: GeofenceAPI{
    private var db = SqliteCRUD()

    init {
        db.connectDatabase()
        fun createTables() {
            val sqlCreateBuildings = "CREATE TABLE IF NOT EXISTS `building` (\n" +
                    "\t`BuildingID`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`Name`\tTEXT NOT NULL UNIQUE\n" +
                    ");"
            val sqlCreateRooms = "CREATE TABLE IF NOT EXISTS `room` (\n" +
                    "\t`RoomID`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`Name`\tTEXT NOT NULL,\n" +
                    "\t`BuildingID`\tINTEGER NOT NULL\n" +
                    ");"
            val sqlCreateNetworkData = "CREATE TABLE IF NOT EXISTS `network_data` (\n" +
                    "\t`NetworkID`\tINTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t`SSID`\tTEXT NOT NULL,\n" +
                    "\t`BSSID`\tTEXT NOT NULL,\n" +
                    "\t`Min`\tINTEGER NOT NULL,\n" +
                    "\t`Max`\tINTEGER NOT NULL,\n" +
                    "\t`RoomID`\tINTEGER NOT NULL\n" +
                    ");"
            db.sqlExecute(sqlCreateBuildings)
            db.sqlExecute(sqlCreateRooms)
            db.sqlExecute(sqlCreateNetworkData)
        }
        createTables()
    }

    override fun addBuilding(buildingName: String): Boolean {
        val connection = db.connection()

        val sql = "INSERT INTO building(Name) VALUES (?);"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, buildingName)

        try {
            statement.executeUpdate()
            connection.close()
            return true
        } catch (ex: SQLException) {
            print("Error: Cannot add building. ")
            when (ex.errorCode) {
                5 -> println("Database locked")
                19 -> println("Building already exists.")
                else -> println(ex.message + ".")
            }
        }

        connection.close()
        return false
    }

    override fun addRoom(buildingName: String, roomName: String): Boolean {
        // Get building id
        val buildingID: Int = getBuildingID(buildingName)
        if (buildingID == -1) return false

        // Check if room exists in building
        val roomID: Int = getRoomID(buildingName, roomName)
        if (roomID != -1) return false

        // Add room given building
        val sql = "INSERT INTO room(Name, BuildingID) VALUES (?, $buildingID);"
        val statement = db.connection().prepareStatement(sql)
        statement.setString(1, roomName)
        try {
            statement.executeUpdate()
            return true
        }
        catch (ex: SQLException) {
            print("Error; Cannot add room")
            when (ex.errorCode) {
                5 -> println("Database locked")
                19 -> println("Error: Cannot add room.")
            }
        }

        return false
    }

    override fun removeBuilding(buildingName: String): Boolean {
        val buildingID = getBuildingID(buildingName)
        if (buildingID == -1) return false

        // Delete all associated rooms
        val sqlDeleteRooms = "DELETE FROM room WHERE BuildingID=$buildingID;"
        db.sqlExecute(sqlDeleteRooms)

        // Delete building
        val sqlDeleteBuilding = "DELETE FROM building WHERE BuildingID=$buildingID;"
        db.sqlExecute(sqlDeleteBuilding)

        return true
    }

    override fun removeRoom(buildingName: String, roomName: String): Boolean {
        val roomID = getRoomID(buildingName, roomName)
        if (roomID == -1) return false

        val sql = "DELETE FROM room WHERE RoomID=$roomID;"
        db.sqlExecute(sql)

        return true
    }

    override fun listBuildings():List<String> {
        val sql = "SELECT Name FROM building;"
        val result = db.sqlExecuteQuery(sql)
        val buildingsList:ArrayList<String> = ArrayList()
        while (result.next()) {
            buildingsList.add(result.getString("Name"))
        }
        return buildingsList.toList()
    }

    override fun listRooms(buildingName: String): List<String> {
        // Get building id
        val buildingID = getBuildingID(buildingName)
        if (buildingID == -1) return Collections.emptyList()

        val sql = "SELECT Name from room WHERE BuildingID=$buildingID;"
        val result = db.sqlExecuteQuery(sql)
        val roomList:ArrayList<String> = ArrayList()
        while (result.next()) {
            roomList.add(result.getString("Name"))
        }
        return roomList.toList()
    }

    override fun editBuildingName(currentName: String, newName: String): Boolean {
        val buildingID = getBuildingID(currentName)
        if (buildingID == -1) return false

        val connection = db.connection()
        val sql = "UPDATE building SET Name=? WHERE BuildingID=$buildingID;"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, newName)

        statement.executeUpdate()
        connection.close()
        return true
    }

    override fun editRoomName(buildingName: String, currentName: String, newName: String): Boolean {
        val roomID = getRoomID(buildingName, currentName)
        if (roomID == -1) return false

        val connection = db.connection()
        val sql = "UPDATE room SET Name=? WHERE RoomID=$roomID;"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, newName)

        statement.executeUpdate()
        connection.close()
        return true
    }

    override fun learnRoom(buildingName: String, roomName: String, scanSummary: List<NetworkRecorder.NetworkCharacteristics>) {
        for (network in scanSummary) {
            val roomID = getRoomID(buildingName, roomName)
            val networkID = getNetworkID(buildingName,roomName, network.bssid)

            var sql:String
            var statement:PreparedStatement

            if (networkID == -1) {
                // Network doesn't exist for room, add network
                val connection = db.connection()

                sql = "INSERT INTO network_data(SSID, BSSID, Min, Max, RoomID) VALUES(?, ?, ?, ?, ?);"
                statement = connection.prepareStatement(sql)
                statement.setString(1, network.ssid)
                statement.setString(2, network.bssid)
                statement.setInt(3, network.minSignal)
                statement.setInt(4, network.maxSignal)
                statement.setInt(5, roomID)

                statement.executeUpdate()
                connection.close()
            }
            else {
                // Update network

                // Get Min and Max
                val connection = db.connection()
                sql = "SELECT Min, Max FROM network_data WHERE NetworkID=$networkID;"
                statement = connection.prepareStatement(sql)
                val result = statement.executeQuery()

                val oldMinSignal = result.getInt("Min")
                val oldMaxSignal = result.getInt("Max")
                connection.close()

                val newMin = min(network.minSignal, oldMinSignal)
                val newMax = max(network.maxSignal, oldMaxSignal)

                // Publish new Min and Max
                sql = "UPDATE network_data SET Min=$newMin, Max=$newMax WHERE NetworkID=$networkID;"
                db.sqlExecute(sql)

            }
        }
    }

    override fun resetRoom(buildingName: String, roomName: String) {
        val roomID = getRoomID(buildingName, roomName)
        if (roomID == -1) return

        val sql = "DELETE FROM network_data WHERE RoomID=$roomID;"
        db.sqlExecute(sql)
    }

    override fun relearnRoom(buildingName: String, roomName: String, scanSummary: List<NetworkRecorder.NetworkCharacteristics>) {
        resetRoom(buildingName, roomName)
        learnRoom(buildingName, roomName, scanSummary)
    }

    override fun findRoomCandidates(scan: List<NetworkScanner.Network>): List<String> {
        val sqlConditions = ArrayList<String>()
        scan.mapTo(sqlConditions) { "(network_data.SSID=? AND " + it.signal + " BETWEEN network_data.Min AND network_data.Max)" }

        // To avoid sql injection because of name, prepared statement for ssid is used
        val sqlConditionsString = sqlConditions.toList().joinToString(" OR ")

        val connection = db.connection()
        val sql = "SELECT building.Name as BuildingName, room.Name as RoomName, count(*) as Frequency FROM room\n" +
                "INNER JOIN building\n" +
                "ON building.BuildingID=room.BuildingID\n" +
                "INNER JOIN network_data\n" +
                "ON network_data.RoomID=room.RoomID\n" +
                "WHERE " + sqlConditionsString + "\n" +
                "GROUP BY room.Name\n" +
                "ORDER BY Frequency DESC\n"
        val statement = connection.prepareStatement(sql)
        for (i in 0 until scan.size) {
            statement.setString(i+1, scan[i].ssid)
        }

        val result = statement.executeQuery()
        val roomCandidates = ArrayList<String>()
        try {
            while (result.next()) {
                val buildingName = result.getString("BuildingName")
                val roomName = result.getString("RoomName")
                val frequency = result.getInt("Frequency")

                roomCandidates.add(roomName)
            }
        } catch (ex: SQLException) {
            println("Error: No results found")
        }

        return roomCandidates.toList()
    }

    private fun getBuildingID(buildingName: String): Int {
        var buildingID:Int = -1

        val connection = db.connection()
        val sql = "SELECT BuildingID FROM building WHERE Name=?"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, buildingName)
        val result = statement.executeQuery()

        try {
            buildingID = result.getInt("BuildingID")
        } catch (ex: SQLException) {
            println("Warning: Building does not exist")
        }

        connection.close()
        return buildingID
    }

    // Returns room id if exist given building
    private fun getRoomID(buildingName: String, roomName: String): Int {
        val connection = db.connection()

        var roomID = -1

        val sql = "SELECT room.RoomID FROM room\n" +
                "INNER JOIN building ON building.BuildingID=room.BuildingID\n" +
                "WHERE building.Name=? AND room.Name=?;"
        val statement  = connection.prepareStatement(sql)
        statement.setString(1, buildingName)
        statement.setString(2, roomName)
        val result = statement.executeQuery()

        try {
            roomID = result.getInt("RoomID")
        }
        catch (ex: SQLException) {
            println("Warning: Room or building does not exist")
        }

        connection.close()
        return roomID
    }

    private fun getNetworkID(buildingName: String, roomName: String, bssid: String): Int {
        val connection = db.connection()

        var networkID = -1

        val sql = "SELECT NetworkID FROM network_data\n" +
                "INNER JOIN room ON network_data.RoomID=room.RoomID\n" +
                "INNER JOIN building ON room.BuildingID=building.BuildingID\n" +
                "WHERE building.Name=? AND room.Name=? AND network_data.BSSID=?;"
        val statement = connection.prepareStatement(sql)
        statement.setString(1, buildingName)
        statement.setString(2, roomName)
        statement.setString(3, bssid)
        val result = statement.executeQuery()

        try {
            networkID = result.getInt("NetworkID")
        }
        catch (ex: SQLException) {
            println("Warning: network, room, or building does not exist")
        }

        connection.close()
        return networkID
    }
}