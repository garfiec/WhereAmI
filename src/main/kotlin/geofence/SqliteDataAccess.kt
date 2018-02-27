package geofence

import network.NetworkRecorder

class SqliteDataAccess: GeofenceAPI{
    private var db = SqliteCRUD()

    init {
        db.connectDatabase()
        fun createTables() {
            val sql = "CREATE TABLE IF NOT EXISTS `building` (\n" +
                    "  `BuildingID` int(11) PRIMARY KEY,\n" +
                    "  `BuildingName` text NOT NULL\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS `room` (\n" +
                    "  `RoomID` int(11) PRIMARY KEY,\n" +
                    "  `RoomName` text NOT NULL,\n" +
                    "  `BuildingID` int(11) NOT NULL\n" +
                    ");\n" +
                    "\n" +
                    "CREATE TABLE IF NOT EXISTS `network_data` (\n" +
                    "  `NetworkID` int(11) PRIMARY KEY,\n" +
                    "  `SSID` text NOT NULL,\n" +
                    "  `BSSID` text NOT NULL,\n" +
                    "  `Min` int(11) NOT NULL,\n" +
                    "  `Max` int(11) NOT NULL,\n" +
                    "  `RoomID` int(11) NOT NULL\n" +
                    ");\n"
            db.sqlExecute(sql)
        }
    }
    override fun addBuilding(name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun addRoom(name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeBuilding(name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeRoom(name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listBuildings() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun listRooms(buildingName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editBuildingName(currentName: String, newName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun editRoomName(currentName: String, newName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun associateRoom(roomName: String, buildingName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disassociateRoom(roomName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun learnRoom(roomName: String, scanSummary: List<NetworkRecorder.NetworkCharacteristics>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun relearnRoom(roomName: String, scanSummary: List<NetworkRecorder.NetworkCharacteristics>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetRoom(roomName: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}