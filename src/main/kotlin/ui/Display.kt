/*
*
* DANGER: The follow code is not suitable for viewing by mortal eyes.
*         Close this file and get on with your day. You have been warned.
*
* */

package ui

import geofence.GeofenceAPI
import geofence.SqliteDataAccess
import network.NetworkRecorder
import network.platform.LinuxNetworkScanner
import java.awt.BorderLayout
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.table.DefaultTableModel

class Display : JFrame("Where Am I") {
    private val scheduledPool: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    private val geofenceAPI:GeofenceAPI = SqliteDataAccess()
    private val networkRecorder = NetworkRecorder()
    private val networkScanner = LinuxNetworkScanner()

    private val titleLabel = JLabel()
    private val buildingsList: JList<Any> = JList()
    private val roomsList: JList<Any> = JList()
    private val networksList = JTable()
    private val networksListTM:DefaultTableModel = networksList.model as DefaultTableModel

    private var isLearning = false

    init {
        contentPane.layout = BorderLayout()
        createMenu()
        createTitleUI()
        createFencesPanel()
        createNetworksPanel()

        initializeUI()

        val updater = Runnable {
            if (!isLearning) updateTitle()
        }
        scheduledPool.scheduleAtFixedRate(updater, 0, 2000, TimeUnit.MILLISECONDS)

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(1200, 800)
        isVisible = true
    }

    private fun createMenu() {
        val menuBar = JMenuBar()

        var menu:JMenu
        var menuItem:JMenuItem

        menu = JMenu("File")
        menuBar.add(menu)

        menuItem = JMenuItem("Exit")
        menuItem.addActionListener { System.exit(0) }
        menu.add(menuItem)


        this.jMenuBar = menuBar
    }

    private fun createTitleUI() {
        val titlePanel = JPanel(BorderLayout())
        titlePanel.border = LineBorder.createGrayLineBorder()

        val titlePadding = JPanel(BorderLayout())
        titlePadding.border = EmptyBorder(8, 8, 5, 5)

        titleLabel.verticalAlignment = JLabel.CENTER

        titlePadding.add(titleLabel, BorderLayout.CENTER)
        titlePanel.add(titlePadding, BorderLayout.CENTER)
        this.add(titlePanel, BorderLayout.NORTH)

    }

    private fun createFencesPanel() {
        val fencePanel = JPanel(BorderLayout())
        fencePanel.border = EmptyBorder(8, 8, 5, 5)

        // Buildings
        val buildingsPanel = JPanel(BorderLayout())
        buildingsPanel.border = EmptyBorder(0, 0, 0, 5)
        val buildingsLabel = JLabel("Buildings")
        buildingsList.addListSelectionListener {
            if (it.valueIsAdjusting) updateRooms()
        }
        buildingsList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val buildingsOptionsPanel = JPanel()
        val buildingsOptionsLayout = BoxLayout(buildingsOptionsPanel, BoxLayout.LINE_AXIS)
        buildingsOptionsPanel.layout = buildingsOptionsLayout
        val buildingsAddButton = JButton("Add")
        buildingsAddButton.addActionListener {
            val buildingName: String? = JOptionPane.showInputDialog(this, "Enter building name")
            if (buildingName != null) {
                val result = geofenceAPI.addBuilding(buildingName!!)
                if (!result) JOptionPane.showMessageDialog(this, "Building already exists")
                updateBuildings()
            }
        }
        val buildingsRemoveButton = JButton("Remove")
        buildingsRemoveButton.addActionListener {
            if (buildingsList.selectedIndex != -1) geofenceAPI.removeBuilding(buildingsList.selectedValue.toString())
            updateBuildings()
        }
        val buildingsRenameButton = JButton("Rename")
        buildingsRenameButton.addActionListener {
            if (buildingsList.selectedIndex != -1) {
                val oldBuildingName = buildingsList.selectedValue.toString()
                val newBuildingName: String? = JOptionPane.showInputDialog(this, "Current name is $oldBuildingName. Enter new building name")
                if (newBuildingName != null) {
                    val result = geofenceAPI.editBuildingName(oldBuildingName, newBuildingName)
                    if (!result) JOptionPane.showMessageDialog(this, "Cannot use building name: $newBuildingName")
                    updateBuildings()
                }
            }
        }
        buildingsOptionsPanel.add(buildingsAddButton)
        buildingsOptionsPanel.add(buildingsRemoveButton)
        buildingsOptionsPanel.add(buildingsRenameButton)

        val buildingsListScroll = JScrollPane()
        buildingsListScroll.setViewportView(buildingsList)

        buildingsPanel.add(buildingsLabel, BorderLayout.NORTH)
        buildingsPanel.add(buildingsListScroll, BorderLayout.CENTER)
        buildingsPanel.add(buildingsOptionsPanel, BorderLayout.SOUTH)

        // Rooms
        val roomsPanel = JPanel(BorderLayout())
        val roomsLabel = JLabel("Rooms")
        roomsList.addListSelectionListener {
            if (it.valueIsAdjusting) updateNetworks()
        }
        roomsList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        val roomsOptionsPanel = JPanel()
        val roomsOptionsLayout = BoxLayout(roomsOptionsPanel, BoxLayout.LINE_AXIS)
        roomsOptionsPanel.layout = roomsOptionsLayout
        val roomsAddButton = JButton("Add")
        roomsAddButton.addActionListener {
            if (buildingsList.selectedIndex != -1) {
                val buildingName = buildingsList.selectedValue.toString()
                val roomName: String? = JOptionPane.showInputDialog(this, "Enter room name")
                if (roomName != null) {
                    val result = geofenceAPI.addRoom(buildingName, roomName!!)
                    if (!result) JOptionPane.showMessageDialog(this, "Room already exists")
                    updateRooms()
                }
            }
        }
        val roomsRemoveButton = JButton("Remove")
        roomsRemoveButton.addActionListener {
            if (buildingsList.selectedIndex != -1 && roomsList.selectedIndex != -1) {
                val buildingName = buildingsList.selectedValue.toString()
                geofenceAPI.removeRoom(buildingName, roomsList.selectedValue.toString())
                updateRooms()
            }
        }
        val roomsRenameButton = JButton("Rename")
        roomsRenameButton.addActionListener {
            if (buildingsList.selectedIndex != -1 && roomsList.selectedIndex != -1) {
                val buildingName = buildingsList.selectedValue.toString()
                val oldRoomName = roomsList.selectedValue.toString()
                val newRoomName: String? = JOptionPane.showInputDialog(this, "Current name is $oldRoomName. Enter new building name")
                if (newRoomName != null) {
                    val result = geofenceAPI.editRoomName(buildingName, oldRoomName, newRoomName)
                    if (!result) JOptionPane.showMessageDialog(this, "Cannot use building name: $newRoomName")
                    updateRooms()
                }
            }
        }
        roomsOptionsPanel.add(roomsAddButton)
        roomsOptionsPanel.add(roomsRemoveButton)
        roomsOptionsPanel.add(roomsRenameButton)

        val roomsListScroll = JScrollPane()
        roomsListScroll.setViewportView(roomsList)

        roomsPanel.add(roomsLabel, BorderLayout.NORTH)
        roomsPanel.add(roomsListScroll, BorderLayout.CENTER)
        roomsPanel.add(roomsOptionsPanel, BorderLayout.SOUTH)

        fencePanel.add(buildingsPanel, BorderLayout.WEST)
        fencePanel.add(roomsPanel, BorderLayout.EAST)
        this.add(fencePanel, BorderLayout.WEST)
    }

    private fun createNetworksPanel() {

        val networksPanel = JPanel(BorderLayout())
        networksPanel.border = EmptyBorder(8, 8, 5, 5)

        val networksLabel = JLabel("Networks")

        networksListTM.addColumn("SSID")
        networksListTM.addColumn("BSSID")
        networksListTM.addColumn("Min Signal")
        networksListTM.addColumn("Max Signal")
        networksListTM.addColumn("Delta Signal")

        val roomsActionsPanel = JPanel()
        val roomsActionsLayout = BoxLayout(roomsActionsPanel, BoxLayout.LINE_AXIS)
        roomsActionsPanel.layout = roomsActionsLayout

        val startLearningButton = JButton("Start Recording")
        startLearningButton.addActionListener {
            if (buildingsList.selectedIndex != -1 && roomsList.selectedIndex != -1 && !isLearning) {
                isLearning = true
                buildingsList.isEnabled = false
                roomsList.isEnabled = false
                this.title = "Where Am I - Learning Room"

                networkRecorder.startRecord()
            }
        }

        val stopLearningButton = JButton("Stop Recording")
        stopLearningButton.addActionListener {
            if (buildingsList.selectedIndex != -1 && roomsList.selectedIndex != -1 && isLearning) {
                networkRecorder.stopRecord()
                val summary = networkRecorder.getSummary()
                geofenceAPI.learnRoom(buildingsList.selectedValue.toString(), roomsList.selectedValue.toString(), summary)

                isLearning = false
                buildingsList.isEnabled = true
                roomsList.isEnabled = true
                this.title = "Where Am I"
                updateNetworks()
            }
        }

        val resetRoomButton = JButton("Reset Room")
        resetRoomButton.addActionListener {
            if (buildingsList.selectedIndex != -1 && roomsList.selectedIndex != -1 && !isLearning) {
                geofenceAPI.resetRoom(buildingsList.selectedValue.toString(), roomsList.selectedValue.toString())
                updateNetworks()
            }
        }

        roomsActionsPanel.add(startLearningButton)
        roomsActionsPanel.add(stopLearningButton)
        roomsActionsPanel.add(resetRoomButton)

        val networkListScroll = JScrollPane()
        networkListScroll.setViewportView(networksList)

        networksPanel.add(networksLabel, BorderLayout.NORTH)
        networksPanel.add(networkListScroll, BorderLayout.CENTER)
        networksPanel.add(roomsActionsPanel, BorderLayout.SOUTH)
        this.add(networksPanel, BorderLayout.CENTER)
    }

    private fun initializeUI() {
        isLearning = false

        updateTitle()
        updateBuildings()
        updateRooms()
        updateNetworks()

        buildingsList.clearSelection()
        roomsList.clearSelection()
        networksList.clearSelection()
    }

    private fun updateTitle() {
        val candidates = geofenceAPI.findRoomCandidates(networkScanner.scan())
        if (candidates.isNotEmpty()) {
            val firstCandidate = candidates[0]
            val building = firstCandidate.BuildingName
            val room = firstCandidate.RoomName
            val confidence = firstCandidate.Confidence
            println("You are certain to be at $building in room $room with a confidence of $confidence%")
            titleLabel.text = "You are certain to be at $building in room $room with a confidence of $confidence%"
        }
        else {
            titleLabel.text = "Your location is undetermined"
        }
    }

    private fun updateBuildings() {
        buildingsList.setListData(geofenceAPI.listBuildings().toTypedArray())
    }

    private fun updateRooms() {
        if (buildingsList.selectedIndex == -1) {
            // Clear list
            roomsList.setListData(arrayOf<String>())
        } else {
            // Update List
            val building = buildingsList.selectedValue.toString()
            roomsList.setListData(geofenceAPI.listRooms(building).toTypedArray())
        }
    }

    private fun updateNetworks() {
        networksListTM.rowCount = 0
        if (buildingsList.selectedIndex != -1) {
            val building = buildingsList.selectedValue.toString()
            val room = roomsList.selectedValue.toString()
            val networks = geofenceAPI.getRoomNetworkData(building, room)

            for (network in networks) {
                val newRow = Vector<String>()
                newRow.add(network.ssid)
                newRow.add(network.bssid)
                newRow.add(network.minSignal.toString())
                newRow.add(network.maxSignal.toString())
                newRow.add((network.maxSignal-network.minSignal).toString())
                networksListTM.addRow(newRow)
            }
        }
    }
}

/*
*
* You chose to ignore the warning above.
* Increment the following counter if your eyes were burned reading this code.
* My condolences.
*
* R.I.P. Counter: 99
*
* */