package de.saring.exerciseviewer.gui.panels

import de.saring.exerciseviewer.data.EVExercise
import de.saring.exerciseviewer.gui.EVContext
import de.saring.exerciseviewer.gui.EVDocument
import de.saring.leafletmap.ColorMarker
import de.saring.leafletmap.ControlPosition
import de.saring.leafletmap.LatLong
import de.saring.leafletmap.LeafletMapView
import de.saring.leafletmap.MapConfig
import de.saring.leafletmap.MapLayer
import de.saring.leafletmap.ScaleControlConfig
import de.saring.leafletmap.ZoomControlConfig
import de.saring.util.unitcalc.FormatUtils
import javafx.concurrent.Worker
import javafx.fxml.FXML
import javafx.geometry.Point2D
import javafx.scene.control.Slider
import javafx.scene.control.Tooltip
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Controller (MVC) class of the "Track" panel, which displays the recorded location data of the exercise (if
 * available) in a map.<br/>
 * The map component is LeafletMap which is based on the Leaflet Javascript library, the data provider is OpenStreetMap.
 *
 * @constructor constructor for dependency injection
 * @param context the ExerciseViewer UI context
 * @param document the ExerciseViewer document / model
 *
 * @author Stefan Saring
 */
class TrackPanelController(
        context: EVContext,
        document: EVDocument) : AbstractPanelController(context, document) {

    private val logger = Logger.getLogger(TrackPanelController::class.java.name)

    @FXML
    private lateinit var spTrackPanel: StackPane

    @FXML
    private lateinit var vbTrackViewer: VBox

    @FXML
    private lateinit var spMapViewer: StackPane

    @FXML
    private lateinit var slPosition: Slider

    private var mapView: LeafletMapView? = null
    private var mapConfig: MapConfig? = null

    private var spMapViewerTooltip: Tooltip? = null
    private var positionMarkerName: String? = null

    /** Flag whether the exercise track has already been shown.  */
    private var showTrackExecuted = false

    override val fxmlFilename: String = "/fxml/panels/TrackPanel.fxml"

    override fun setupPanel() {

        // setup the map viewer if track data is available
        val exercise = document.exercise
        if (exercise.recordingMode.isLocation) {
            setupMapView()
            setupMapViewerTooltip()
            setupTrackPositionSlider()
        } else {
            // remove the track viewer VBox, the StackPane now displays the label "No track data available")
            spTrackPanel.children.remove(vbTrackViewer)
        }
    }

    private fun setupMapView() {
        mapView = LeafletMapView()
        spMapViewer.children.add(mapView)

        val metric = document.options.unitSystem == FormatUtils.UnitSystem.METRIC

        mapConfig = MapConfig(
                listOf(MapLayer.OPENSTREETMAP, MapLayer.OPENCYCLEMAP, MapLayer.HIKE_BIKE_MAP, MapLayer.MTB_MAP),
                ZoomControlConfig(true, ControlPosition.BOTTOM_LEFT),
                ScaleControlConfig(true, ControlPosition.BOTTOM_LEFT, metric))
    }

    private fun setupMapViewerTooltip() {
        spMapViewerTooltip = Tooltip()
        spMapViewerTooltip!!.isAutoHide = true
    }

    private fun setupTrackPositionSlider() {
        // on position slider changes: update position marker in the map viewer and display tooltip with details
        // (slider uses a double value, make sure the int value has changed)
        slPosition.valueProperty().addListener { _, oldValue, newValue ->
            if (oldValue.toInt() != newValue.toInt()) {
                movePositionMarker(newValue.toInt())
            }
        }
    }

    private fun movePositionMarker(positionIndex: Int) {
        val samplePosition = document.exercise.sampleList[positionIndex].position

        // some samples could have no position
        samplePosition?.let {
            val position = LatLong(it.latitude, it.longitude)

            if (positionMarkerName == null) {
                positionMarkerName = mapView!!.addMarker(position, "", ColorMarker.BLUE_MARKER, 0)
            } else {
                mapView!!.moveMarker(positionMarkerName!!, position)
            }

            val tooltipText = createToolTipText(positionIndex)
            spMapViewerTooltip!!.text = tooltipText

            // display position tooltip in the upper left corner of the map viewer container
            var tooltipPos = spMapViewer.localToScene(8.0, 8.0)
            tooltipPos = tooltipPos.add(getMapViewerScreenPosition())
            spMapViewerTooltip!!.show(spMapViewer, tooltipPos.x, tooltipPos.y)
        }
    }

    /**
     * Displays map and the track of the current exercise, if available. This method will be executed only once and
     * should be called when the user wants to see the track (to prevent long startup delays).
     */
    fun showMapAndTrack() {
        if (!showTrackExecuted) {
            showTrackExecuted = true

            val exercise = document.exercise
            if (exercise.recordingMode.isLocation) {

                // display map, on success display the track and laps
                mapView!!.displayMap(mapConfig!!).whenComplete { workerState, throwable ->

                    if (workerState == Worker.State.SUCCEEDED) {
                        showTrackAndLaps()
                        // enable position slider by setting max. sample count
                        slPosition.max = (exercise.sampleList.size - 1).toDouble()
                    } else if (throwable != null) {
                        logger.log(Level.SEVERE, "Failed to display map!", throwable)
                    }
                }
            }
        }
    }

    private fun showTrackAndLaps() {
        val exercise = document.exercise
        val samplePositions = createSamplePositionList(exercise)

        if (!samplePositions.isEmpty()) {
            mapView!!.addTrack(samplePositions)

            // display lap markers first, start and end needs to be displayed on top
            val lapPositions = createLapPositionList(exercise)
            for (i in lapPositions.indices) {
                mapView!!.addMarker(lapPositions[i],
                        context.resources.getString("pv.track.maptooltip.lap", i + 1),
                        ColorMarker.GREY_MARKER, 0)
            }

            mapView!!.addMarker(samplePositions.first(),
                    context.resources.getString("pv.track.maptooltip.start"),
                    ColorMarker.GREEN_MARKER, 1000)
            mapView!!.addMarker(samplePositions.last(),
                    context.resources.getString("pv.track.maptooltip.end"),
                    ColorMarker.RED_MARKER, 2000)
        }
    }

    private fun createSamplePositionList(exercise: EVExercise): List<LatLong> =
            exercise.sampleList
                    .filter { it.position != null }
                    .map { LatLong(it.position!!.latitude, it.position!!.longitude) }
                    .toList()

    private fun createLapPositionList(exercise: EVExercise): List<LatLong> {
        val lapPositions = mutableListOf<LatLong>()

        // ignore last lap split position, it's the exercise end position
        for (i in 0..exercise.lapList.size - 1 - 1) {
            val position = exercise.lapList[i].positionSplit
            if (position != null) {
                lapPositions.add(LatLong(position.latitude, position.longitude))
            }
        }
        return lapPositions
    }

    private fun getMapViewerScreenPosition(): Point2D {
        val scene = spMapViewer.scene
        val window = scene.window
        return Point2D(scene.x + window.x, scene.y + window.y)
    }

    /**
     * Creates the tool tip text for the specified exercise sample to be shown on the map.
     *
     * @param sampleIndex index of the exercise sample
     * @return text
     */
    private fun createToolTipText(sampleIndex: Int): String {

        val exercise = document.exercise
        val sample = exercise.sampleList[sampleIndex]
        val formatUtils = context.formatUtils

        val sb = StringBuilder()
        appendToolTipLine(sb, "pv.track.tooltip.trackpoint", (sampleIndex + 1).toString())

        sample.timestamp?.let {
            appendToolTipLine(sb, "pv.track.tooltip.time", FormatUtils.seconds2TimeString((it / 1000).toInt()))
        }
        sample.distance?.let {
            appendToolTipLine(sb, "pv.track.tooltip.distance", formatUtils.distanceToString((it / 1000f).toDouble(), 3))
        }
        sample.altitude?.let {
            appendToolTipLine(sb, "pv.track.tooltip.altitude", formatUtils.heightToString(it.toInt()))
        }
        sample.heartRate?.let {
            appendToolTipLine(sb, "pv.track.tooltip.heartrate", formatUtils.heartRateToString(it.toInt()))
        }
        sample.speed?.let {
            appendToolTipLine(sb, "pv.track.tooltip.speed", formatUtils.speedToString(it, 2, document.speedMode))
        }
        sample.temperature?.let {
            appendToolTipLine(sb, "pv.track.tooltip.temperature", formatUtils.temperatureToString(it))
        }
        return sb.toString()
    }

    private fun appendToolTipLine(sb: StringBuilder, resourceKey: String, value: String) =
            sb.append("${context.resources.getString(resourceKey)}: $value\n")
}
