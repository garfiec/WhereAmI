package network.platform
import network.NetworkScanner
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

class LinuxNetworkScanner: NetworkScanner {
    private val dataPattern = Pattern.compile("([^:]*):(.{22}):(\\d*)")

    private var networks = ArrayList<NetworkScanner.Network>()

    override fun scan(): ArrayList<NetworkScanner.Network> {
        val runtime = Runtime.getRuntime()
        val process = runtime.exec("nmcli -f ssid,bssid,signal -t device wifi")
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

        for (line in bufferedReader.lines()) {
            val network = extractData(line)
            networks.add(network)
        }

        return networks
    }

    private fun extractData(line: String): NetworkScanner.Network {
        val networkMatch = dataPattern.matcher(line)
        networkMatch.matches()

        if (networkMatch.groupCount() != 3) return NetworkScanner.Network()

        val ssid = networkMatch.group(1)
        val bssid = networkMatch.group(2).replace("\\", "")
        val signal = networkMatch.group(3).toInt()

        return NetworkScanner.Network(ssid, bssid, signal)
    }

}

fun main(args: Array<String>) {

}