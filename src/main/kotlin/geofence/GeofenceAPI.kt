package geofence

interface GeofenceAPI {
    // Fences
    fun addBuilding()           // Add a new building entry
    fun addRoom()               // Add a new room entry
    fun removeBuilding()        // Remove a building entry
    fun removeRoom()            // Remove a room entry
    fun listBuildings()         // Get list of all building entries
    fun listRooms()             // Get list of rooms given building name

    // Fence configurations
    fun editBuildingName()      // Change name to building
    fun editRoomName()          // Change name to room
    fun associateRoom()         // Associate to building
    fun disassociateRoom()      // Removes association to room

    // Fence data-set
    fun learnRoom()             // Add new or update set
    fun relearnRoom()           // Delete all and replace
    fun resetRoom()             // Delete all room data
}