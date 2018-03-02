package network

import network.platform.LinuxNetworkScanner
import java.util.concurrent.*

class NetworkRecorder {
    data class NetworkCharacteristics(var ssid: String, val bssid: String, var minSignal: Int, var maxSignal: Int)

    private var isRecording = false

    private var scheduledPool:ScheduledExecutorService = Executors.newScheduledThreadPool(0)
    private var scanSummary = HashMap<String, NetworkCharacteristics>()

    private val recordRoutine = Runnable {
        val networkScan = LinuxNetworkScanner().scan()
        for (newNetwork in networkScan) {
            // Filter out networks with weak signal
            if (newNetwork.signal >= 25) {
                if (scanSummary.containsKey(newNetwork.bssid)) {
                    val network = scanSummary[newNetwork.bssid]
                    // Check lower bound
                    if (newNetwork.signal < network!!.minSignal) network.minSignal = newNetwork.signal

                    // check upper bound
                    if (newNetwork.signal > network!!.maxSignal) network.maxSignal = newNetwork.signal
                }
                else {
                    scanSummary[newNetwork.bssid] = NetworkCharacteristics(ssid = newNetwork.ssid, bssid = newNetwork.bssid, minSignal = newNetwork.signal, maxSignal = newNetwork.signal)

                }
            }
        }
    }

    fun startRecord() {
        if (!isRecording) {
            isRecording = true
            scheduledPool = Executors.newScheduledThreadPool(1)
            scheduledPool.scheduleAtFixedRate(recordRoutine, 0, 250, TimeUnit.MILLISECONDS)
        }
    }

    fun stopRecord() {
        isRecording = false
        scheduledPool.shutdown()
    }

    fun getSummary(): List<NetworkCharacteristics> {
        return scanSummary.values.toList()
    }
}