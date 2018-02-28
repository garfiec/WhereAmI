import geofence.SqliteDataAccess
import network.NetworkRecorder
import network.NetworkScanner

fun main(args: Array<String>) {
    val data = SqliteDataAccess()
    val networkScanner = NetworkScanner()
    val candidates = data.findRoomCandidates(networkScanner.scan())
    for (candidate in candidates) {
        println(candidate)
    }

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

//    for (network in summary) {
//        println("SSID: " + network.ssid + " | BSSID: " + network.bssid + " | MIN SIGNAL: " + network.minSignal.toString() + " | MAX SIGNAL: " + network.maxSignal.toString() + " | Delta: " + (network.maxSignal - network.minSignal).toString())
//    }

}
