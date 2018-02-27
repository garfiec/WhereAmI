package geofence

import network.NetworkRecorder

interface GeofenceAPI {
    // Fences
    fun addBuilding(buildingName: String)       // Add a new building entry
    fun addRoom(roomName: String)               // Add a new room entry
    fun removeBuilding(buildingName: String)    // Remove a building entry
    fun removeRoom(roomName: String)            // Remove a room entry
    fun listBuildings()                         // Get list of all building entries
    fun listRooms(buildingName: String)         // Get list of rooms given building name

    // Fence configurations
    fun editBuildingName(currentName: String, newName: String)      // Change name to building
    fun editRoomName(currentName: String, newName: String)          // Change name to room
    fun associateRoom(roomName: String, buildingName: String)       // Associate to building
    fun disassociateRoom(roomName: String)                          // Removes association to room

    // Fence data-set
    fun learnRoom(roomName: String, scanSummary: List<NetworkRecorder.NetworkCharacteristics>)      // Add new or update set
    fun relearnRoom(roomName: String, scanSummary: List<NetworkRecorder.NetworkCharacteristics>)    // Delete all and replace
    fun resetRoom(roomName: String)                                                                 // Delete all room data
}