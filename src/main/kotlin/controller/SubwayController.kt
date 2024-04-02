package controller

import model.MutableSubwayModel
import view.SubwayView
import java.io.IOException
import kotlin.system.exitProcess

/**
 * Coordinates a subway view and subway model to control the MBTA routing
 * program.
 */
interface SubwayController {
    /**
     * Starts, and continues running, the program, accepting commands and
     * displaying output.
     */
    fun start()
}

/**
 * Coordinates a subway view and subway model to control the MBTA routing
 * program. Supports multiple commands to output different data from the model,
 * and outputs other useful information through the view.
 */
class MbtaSubwayController : SubwayController {
    private val model: MutableSubwayModel
    private val view: SubwayView

    // Whether the program should forcefully quit. This is normally necessary to
    // prevent Retrofit from causing the program to continue running after the
    // user quits, but can be disabled for unit tests.
    private val forceQuit: Boolean

    private val commandInfo = """
        1: Reload the data from the MBTA server.
        2: List the names of all subway routes.
        3: List the subway route with the most stops.
        4: List the subway route with the fewest stops.
        5: List the subway transfer stops (the stops
           connecting multiple subway routes).
        6: Find a route from one stop to another.
        q: Quit the program.
    """.trimIndent()

    // Maps each allowed command to the function that should be executed when
    // the command is inputted. All command names should be lowercase. Each
    // function returns whether the program should quit.
    private val commandMap: Map<String, () -> Boolean> = mapOf(
        "1" to ::reloadData,
        "2" to ::listSubwayRoutes,
        "3" to ::listRouteWithMostStops,
        "4" to ::listRouteWithFewestStops,
        "5" to ::listSubwayTransferStops,
        "6" to ::findPath,
        "q" to ::quit
    )

    /**
     * Creates the controller, which will load route data into the given subway
     * model and conduct input and output operations through the given view.
     */
    constructor(model: MutableSubwayModel, view: SubwayView) {
        this.model = model
        this.view = view
        this.forceQuit = true
    }

    /**
     * Creates the controller, which will load route data into the given subway
     * model and conduct input and output operations through the given view.
     * If forceQuit is disabled, the program may continue running indefinitely
     * if run outside a testing framework.
     */
    internal constructor(model: MutableSubwayModel, view: SubwayView, forceQuit: Boolean) {
        this.model = model
        this.view = view
        this.forceQuit = forceQuit
    }

    /**
     * Starts, and continues running, the program.
     *
     * Loads route data into the controller's associated model, and then
     * repeatedly accepts commands from the user, executes them, and displays
     * their results. Exits the program if a fatal error occurs or if the quit
     * command is entered. Exceptions are displayed as user-friendly error
     * messages.
     */
    override fun start() {
        var shouldQuit = false

        try {
            model.loadRouteData()
            view.renderStringLn("Welcome to the MBTA subway routing program!")
            view.renderStringLn("Subway route data has been successfully loaded.\n")
        } catch (_: IOException) {
            shouldQuit = true
            view.renderStringLn("A fatal error occurred while attempting to load route data")
            view.renderStringLn("from the MBTA server to initialize the program.\n")
        }

        while (!shouldQuit) {
            view.renderStringLn(
                "Please enter one of the following options (before the colon):"
            )
            view.renderStringLn(commandInfo + "\n")

            try {
                val requestedAction = commandMap.getOrDefault(
                    view.inputLine().trim().lowercase(), ::handleInvalidCommand
                )
                view.renderStringLn()
                shouldQuit = requestedAction()
                view.renderStringLn()
            } catch (_: NoSuchElementException) {
                shouldQuit = true
                view.renderStringLn("A fatal error occurred while attempting to read input.\n")
            }
        }
        if (forceQuit) {
            exitProcess(0)
        }
    }

    // Attempts to reload data into the model from the server; displays an error
    // message if this fails. Returns false to indicate that the program should
    // not quit.
    private fun reloadData(): Boolean {
        try {
            model.loadRouteData()
            view.renderStringLn("The route data was successfully reloaded.")
        } catch (_: IOException) {
            view.renderStringLn("An error occurred while attempting to load new route data.")
            view.renderStringLn("The previously loaded data has been retained.\n")
        }
        return false
    }

    // Attempts to display the model's list of subway routes; displays an error
    // message if this fails. Returns false to indicate that the program should
    // not quit.
    private fun listSubwayRoutes(): Boolean {
        try {
            view.renderStringLn("The names of all MBTA subway routes are:")
            view.renderAllSubwayRoutes()
        } catch (_: IllegalStateException) {
            view.renderStringLn("An unexpected error occurred when attempting to access the")
            view.renderStringLn("route data. Please try again.\n")
        }
        return false
    }

    // Attempts to display the subway route with the most stops; displays an
    // error message if this fails. Returns false to indicate that the program
    // should not quit.
    private fun listRouteWithMostStops(): Boolean {
        try {
            view.renderSubwayRouteWithMostStops()
        } catch (_: IllegalStateException) {
            view.renderStringLn("An unexpected error occurred when attempting to access the")
            view.renderStringLn("route data. Please try again.\n")
        }
        return false
    }

    // Attempts to display the subway route with the fewest stops; displays an
    // error message if this fails. Returns false to indicate that the program
    // should not quit.
    private fun listRouteWithFewestStops(): Boolean {
        try {
            view.renderSubwayRouteWithFewestStops()
        } catch (_: IllegalStateException) {
            view.renderStringLn("An unexpected error occurred when attempting to access the")
            view.renderStringLn("route data. Please try again.\n")
        }
        return false
    }

    // Attempts to display all stops connecting multiple subway routes; displays
    // an error message if this fails. Returns false to indicate that the
    // program should not quit.
    private fun listSubwayTransferStops(): Boolean {
        try {
            view.renderSubwayTransferStops()
        } catch (_: IllegalStateException) {
            view.renderStringLn("An unexpected error occurred when attempting to access the")
            view.renderStringLn("route data. Please try again.\n")
        }
        return false
    }

    // Attempts to input a source and destination stop from the user and display
    // a path between them; displays an error message if this fails. Returns
    // whether the program should quit based on whether a fatal input error
    // occurred.
    private fun findPath(): Boolean {
        try {
            view.renderStringLn("Please enter the name of the stop to start from:")
            val sourceStopName = view.inputLine().trim()

            view.renderStringLn("\nPlease enter the name of the destination stop:")
            val destStopName = view.inputLine().trim()

            if (sourceStopName.lowercase() == destStopName.lowercase()) {
                view.renderStringLn("\nYou cannot get a route from a station to itself!")
                return false
            }

            view.renderStringLn()
            view.renderPath(sourceStopName, destStopName)
        } catch (_: NoSuchElementException) {
            view.renderStringLn("A fatal error occurred while attempting to read input.\n")
            return true
        } catch (_: IllegalStateException) {
            view.renderStringLn("An unexpected error occurred when attempting to access the")
            view.renderStringLn("route data. Please try again.\n")
        } catch (_: IllegalArgumentException) {
            view.renderStringLn("The stop names provided were not recognized.")
        }
        return false
    }

    // Notifies the user that the program is exiting, and then returns true to
    // indicate that the program should terminate.
    private fun quit(): Boolean {
        view.renderString("The program has terminated.")
        return true
    }

    // Displays an error message to the user since they inputted an unrecognized
    // command. Returns false to indicate that the program should not quit.
    private fun handleInvalidCommand(): Boolean {
        view.renderStringLn("The option entered was not recognized.")
        return false
    }
}