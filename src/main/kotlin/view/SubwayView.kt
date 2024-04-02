package view

import model.Route
import model.SubwayModel
import java.util.Scanner

/**
 * Enables information to be input and output, including the output of data from
 * a subway model.
 */
interface SubwayView {
    /**
     * Outputs the given string.
     */
    fun renderString(msg: String)

    /**
     * Outputs the given string, with a line break following it.
     */
    fun renderStringLn(msg: String = "")

    /**
     * Outputs the names of the subway routes stored by the view's associated
     * subway model. The route names are formatted into a list for display, with
     * a line break following it.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the subway model associated with the view.
     */
    fun renderAllSubwayRoutes()

    /**
     * Outputs the name of the subway route stored by the view's associated
     * subway model that has the most stops, as well as how many stops it has.
     * A line break follows the output.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the subway model associated with the view.
     */
    fun renderSubwayRouteWithMostStops()

    /**
     * Outputs the name of the subway route stored by the view's associated
     * subway model that has the fewest stops, as well as how many stops it has.
     * A line break follows the output.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the subway model associated with the view.
     */
    fun renderSubwayRouteWithFewestStops()

    /**
     * Outputs the name of each subway stop that connects multiple subway
     * routes, as well as the names of the routes connected by each of these
     * stops. A line break follows the output.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the subway model associated with the view.
     */
    fun renderSubwayTransferStops()

    /**
     * Outputs directions to travel by subway from the source station with the
     * given name to the destination station with the given name. A line break
     * follows the output.
     *
     * The outputted directions include the intermediate stops to travel to and
     * the subway routes to ride. The output may be that there is no path from
     * the source to the destination; this will be the result if the provided
     * source and destination represent the same station.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the subway model associated with the view.
     * @throws IllegalArgumentException if either provided station name does not
     * correspond to a stop in the subway model associated with the view.
     */
    fun renderPath(sourceStopName: String, destStopName: String)

    /**
     * Blocks if necessary to read a line of input, and then returns the
     * inputted string (without the line separator character at the end if
     * originally present).
     *
     * @throws NoSuchElementException if the view's input source is unable to
     * provide more input.
     */
    fun inputLine(): String
}

/**
 * Enables text to be input from a Readable and output from an Appendable,
 * including the output of data from a subway model.
 *
 * @param model the subway model from which data should be obtained for display.
 * @param readable the Readable source from which input should be read.
 * @param output the Appendable to which textual output should be appended.
 */
class TextualSubwayView(
    private val model: SubwayModel,
    readable: Readable,
    private val output: Appendable
) : SubwayView {
    private val input: Scanner = Scanner(readable)

    override fun renderString(msg: String) {
        output.append(msg)
    }

    override fun renderStringLn(msg: String) {
        output.append(msg).append('\n')
    }

    override fun renderAllSubwayRoutes() {
        val routeNames = model.getSubwayRoutes().map { it.name }
        renderStringLn(formatList(routeNames))
    }

    override fun renderSubwayRouteWithMostStops() {
        val routePair = model.getSubwayRouteWithMostStops()
        if (routePair == null) {
            renderStringLn("The model has no subway routes.")
        } else {
            val (route, numStops) = routePair
            renderStringLn("The subway route with the most stops is: ${route.name}")
            renderStringLn("This route has $numStops stops.")
        }
    }

    override fun renderSubwayRouteWithFewestStops() {
        val routePair = model.getSubwayRouteWithFewestStops()
        if (routePair == null) {
            renderStringLn("The model has no subway routes.")
        } else {
            val (route, numStops) = routePair
            renderStringLn("The subway route with the fewest stops is: ${route.name}")
            renderStringLn("This route has $numStops stops.")
        }
    }

    override fun renderSubwayTransferStops() {
        val transferStopsMap = model.getTransferStops()
        var routeNames: List<String>

        if (transferStopsMap.isEmpty()) {
            renderStringLn("There are no subway transfer stops.")
        } else {
            renderStringLn(
                "The subway transfer stops, followed by the routes they connect, are:\n"
            )

            transferStopsMap.forEach { (stopName, routesConnected) ->
                routeNames = routesConnected.map { it.name }
                renderStringLn("$stopName: ${formatList(routeNames)}")
            }
        }
    }

    override fun renderPath(sourceStopName: String, destStopName: String) {
        val path = model.findPath(sourceStopName, destStopName)
        var currentRoute: Route? = null

        if (path == null) {
            renderStringLn("A route between these stops could not be calculated.")
        } else {
            for ((route, stopName) in path) {
                if (currentRoute == null) {
                    currentRoute = route
                    renderStringLn(sourceStopName.trim())
                    renderStringLn("~ Board a ${route.name} train. ~")
                } else if (currentRoute != route) {
                    currentRoute = route
                    renderStringLn("~ Transfer to a ${route.name} train. ~")
                }
                renderStringLn("  |\n  |\n  |\n$stopName")
            }
            renderStringLn("\nYou will have arrived at your destination!")
        }
    }

    override fun inputLine(): String {
        return input.nextLine()
    }

    /*
    Returns a String representation of the given list, separating items with
    commas when necessary and providing a final "and". "<none>" is returned
    if the given list is empty.

    Examples:
    [] -> "<none>"
    ["red"] -> "red"
    ["red", "blue"] -> "red and blue"
    ["red", "blue", "green"] -> "red, blue, and green"
     */
    private fun formatList(items: List<String>): String {
        if (items.isEmpty()) {
            return "<none>"
        }

        val outputBuilder = StringBuilder()
        val numItems = items.size

        items.forEachIndexed { i, str ->
            outputBuilder.append(str)
            if (i <= numItems - 3) {
                outputBuilder.append(", ")
            } else if (i == numItems - 2) {
                outputBuilder.append(
                    if (numItems > 2) {
                        ", and "
                    } else {
                        " and "
                    }
                )
            }
        }
        return outputBuilder.toString()
    }
}