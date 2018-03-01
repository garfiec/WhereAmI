package network

interface NetworkScanner {
    data class Network(val ssid: String = "", val bssid: String = "", val signal: Int = 0)

    fun scan(): ArrayList<NetworkScanner.Network>
}