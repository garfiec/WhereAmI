package geofence

import network.NetworkRecorder
import network.NetworkScanner

interface GeofenceAPI {
    data class RoomCandidates(val BuildingName:String, val RoomName: String, val Confidence: Float)

    // Fences
    fun addBuilding(buildingName: String): Boolean                  // Add a new building entry
    fun addRoom(buildingName: String, roomName: String): Boolean    // Add a new room entry
    fun removeBuilding(buildingName: String): Boolean               // Remove a building entry
    fun removeRoom(buildingName: String, roomName: String): Boolean // Remove a room entry
    fun listBuildings():List<String>                                // Get list of all building entries
    fun listRooms(buildingName: String):List<String>                // Get list of rooms given building name

    // Fence configurations
    fun editBuildingName(currentName: String, newName: String): Boolean                     // Change name to building
    fun editRoomName(buildingName: String, currentName: String, newName: String): Boolean   // Change name to room

    // Fence data-set
    fun learnRoom(buildingName: String, roomName: String, scanSummary: List<NetworkRecorder.NetworkCharacteristics>)    // Add new or update set
    fun resetRoom(buildingName: String, roomName: String)                                                               // Delete all room data
    fun relearnRoom(buildingName: String, roomName: String, scanSummary: List<NetworkRecorder.NetworkCharacteristics>)  // Delete all and replace
    fun findRoomCandidates(scan: List<NetworkScanner.Network>): List<RoomCandidates>                                    // Ranks a list of possible rooms
}