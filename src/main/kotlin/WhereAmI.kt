import geofence.SqliteCRUD
import geofence.SqliteDataAccess
import network.NetworkRecorder

fun main(args: Array<String>) {
    val data = SqliteDataAccess()

    val networkRecorder = NetworkRecorder()
    networkRecorder.startRecord()
    while (true) {
        val line = readLine()
        if (line == "stop") {
            break
        }
    }
    networkRecorder.stopRecord()

    val summary = networkRecorder.getSummary()
    for (network in summary) {
        println("SSID: " + network.ssid + " | BSSID: " + network.bssid + " | MIN SIGNAL: " + network.minSignal.toString() + " | MAX SIGNAL: " + network.maxSignal.toString() + " | Delta: " + (network.maxSignal - network.minSignal).toString())
    }

}
