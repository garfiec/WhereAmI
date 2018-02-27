package network
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

class NetworkScanner {
    data class Network(val ssid: String = "", val bssid: String = "", val signal: Int = 0)
    private val dataPattern = Pattern.compile("([^:]*):(.{22}):(\\d*)")

    private var networks = ArrayList<Network>()

    fun scan(): ArrayList<Network> {
        val runtime = Runtime.getRuntime()
        val process = runtime.exec("nmcli -f ssid,bssid,signal -t device wifi")
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

        for (line in bufferedReader.lines()) {
            val network = extractData(line)
            networks.add(network)
        }

        return networks
    }

    private fun extractData(line: String): Network {
        val networkMatch = dataPattern.matcher(line)
        networkMatch.matches()

        if (networkMatch.groupCount() != 3) return Network()

        val ssid = networkMatch.group(1)
        val bssid = networkMatch.group(2).replace("\\", "")
        val signal = networkMatch.group(3).toInt()

        return Network(ssid, bssid, signal)
    }

}

fun main(args: Array<String>) {

}