package model

import model.graph.MbtaTransitGraph
import java.io.IOException

/**
 * Enables the loading, storage, and access of subway route data.
 */
interface MutableSubwayModel : SubwayModel {
    /**
     * Loads subway route data into the model.
     *
     * Should be called before calling any methods to obtain data from the
     * model.
     *
     * @throws IOException if any server- or parsing-related error occurs that
     * prevents valid route data from being obtained.
     */
    fun loadRouteData()
}

/**
 * Stores and provides access to subway route data.
 *
 * Does not allow the model to be mutated, including for the loading of route
 * data.
 *
 * @see MutableSubwayModel
 */
interface SubwayModel {
    /**
     * Gets the model's list of subway routes.
     *
     * The list of routes that was most recently successfully loaded into the
     * model is returned.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model.
     */
    fun getSubwayRoutes(): List<Route>

    /**
     * Returns a pair containing the model's subway route with the most stops,
     * and its number of stops.
     *
     * Returns null if the model has no subway routes.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model.
     */
    fun getSubwayRouteWithMostStops(): Pair<Route, Int>?

    /**
     * Returns a pair containing the model's subway route with the fewest stops,
     * and its number of stops.
     *
     * Returns null if the model has no subway routes.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model.
     */
    fun getSubwayRouteWithFewestStops(): Pair<Route, Int>?

    /**
     * Returns a map from the name of each stop that connects two or more subway
     * routes to the list of routes that it connects.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model.
     */
    fun getTransferStops(): Map<String, List<Route>>

    /**
     * Returns a path from the given source station to the given destination
     * station.
     *
     * The source and destination stations must be given by their names, not
     * their IDs. The path is returned as a list of pairs, where each pair
     * represents (1) the route to take to the next stop and (2) the name of the
     * next stop. (The source station is not included in the returned path.) The
     * path returned is a path requiring the least number of stops to get from
     * the source to the destination.
     *
     * Null is returned if no path can be found from the source station to the
     * destination station. This includes if the source and destination stations
     * are the same.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model.
     * @throws IllegalArgumentException if a station with the given source or
     * destination name cannot be found.
     */
    fun findPath(
        sourceStationName: String, destStationName: String
    ): List<Pair<Route, String>>?
}

/**
 * Enables MBTA subway data to be loaded (using an API caller), stored, and
 * accessed.
 */
class MbtaSubwayModel : MutableSubwayModel {
    private val apiCaller: ApiCaller
    private var routes: List<Route>? = null
    private var subwayGraph: MbtaTransitGraph? = null

    // Maps each route to the number of stops on it.
    private var stopsPerRoute: Map<Route, Int>? = null

    /**
     * Creates the model with a default API caller that will get subway data
     * from the MBTA API.
     */
    constructor() : this(MbtaApiCaller())

    /**
     * Creates the model with the specified API caller that will be used to
     * obtain subway data.
     */
    internal constructor(apiCaller: ApiCaller) {
        this.apiCaller = apiCaller
    }

    /**
     * Loads subway route data into the model using the model's API caller.
     *
     * Should be called before calling any methods to obtain data from the
     * model.
     *
     * @throws IOException if any server- or parsing-related error occurs that
     * prevents valid route data from being obtained.
     */
    override fun loadRouteData() {
        routes = apiCaller.getSubwayRoutes().also {
            subwayGraph = MbtaTransitGraph.fromRoutePatterns(
                apiCaller.getCanonicalRoutePatterns(it)
            )
        }
        populateStopsPerRoute()
    }

    // Populates the stopsPerRoute map using the loaded routes and subway graph.
    private fun populateStopsPerRoute() {
        var route: Route
        val routeStopsMap: MutableMap<Route, Int> = hashMapOf()
        routes?.forEach { routeStopsMap[it] = 0 }
        subwayGraph?.let {
            for (station in it.stationNodes) {
                for (routeId in station.routeIds) {
                    route = getRouteFromId(routeId)
                    routeStopsMap[route] = routeStopsMap.getValue(route) + 1
                }
            }
        }
        stopsPerRoute = routeStopsMap
    }

    // Returns the Route from the model's list of routes with the given ID.
    // Throws an IllegalStateException if the route data has not been
    // successfully loaded into the model yet, or an IllegalArgumentException if
    // a route with the given ID cannot be found.
    private fun getRouteFromId(id: String): Route {
        routes?.let {
            return it.find { route -> route.id == id } ?: throw IllegalArgumentException(
                "The specified route could not be found."
            )
        }
        throw IllegalStateException("The route data has not been loaded yet.")
    }

    /**
     * Gets the model's list of subway routes.
     *
     * The list of routes that was most recently successfully loaded into the
     * model is returned.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model using the [loadRouteData] method.
     */
    override fun getSubwayRoutes(): List<Route> {
        return routes ?: throw IllegalStateException("The route data has not been loaded yet.")
    }

    /**
     * Returns a pair containing the model's subway route with the most stops,
     * and its number of stops.
     *
     * Returns null if the model has no subway routes.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model using the [loadRouteData] method.
     */
    override fun getSubwayRouteWithMostStops(): Pair<Route, Int>? {
        stopsPerRoute?.let {
            val (route, stopCount) = it.maxByOrNull { entry -> entry.value } ?: return null
            return Pair(route, stopCount)
        }
        throw IllegalStateException("The route data has not been loaded yet.")
    }

    /**
     * Returns a pair containing the model's subway route with the fewest stops,
     * and its number of stops.
     *
     * Returns null if the model has no subway routes.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model using the [loadRouteData] method.
     */
    override fun getSubwayRouteWithFewestStops(): Pair<Route, Int>? {
        stopsPerRoute?.let {
            val (route, stopCount) = it.minByOrNull { entry -> entry.value } ?: return null
            return Pair(route, stopCount)
        }
        throw IllegalStateException("The route data has not been loaded yet.")
    }

    /**
     * Returns a map from the name of each stop that connects two or more subway
     * routes to the list of routes that it connects.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model using the [loadRouteData] method.
     */
    override fun getTransferStops(): Map<String, List<Route>> {
        return subwayGraph?.stationNodes?.filter { it.routeIds.size >= 2 }?.associate {
            Pair(it.name, it.routeIds.map { id -> getRouteFromId(id) })
        } ?: throw IllegalStateException("The route data has not been loaded yet.")
    }

    /**
     * Returns a path from the given source station to the given destination
     * station.
     *
     * The source and destination stations must be given by their names, not
     * their IDs. The path is returned as a list of pairs, where each pair
     * represents (1) the route to take to the next stop and (2) the name of the
     * next stop. (The source station is not included in the returned path.) The
     * path returned is a path requiring the least number of stops to get from
     * the source to the destination.
     *
     * Null is returned if no path can be found from the source station to the
     * destination station. This includes if the source and destination stations
     * are the same.
     *
     * @throws IllegalStateException if route data has not yet been successfully
     * loaded into the model using the [loadRouteData] method.
     * @throws IllegalArgumentException if a station with the given source or
     * destination name cannot be found.
     */
    override fun findPath(
        sourceStationName: String,
        destStationName: String
    ): List<Pair<Route, String>>? {
        subwayGraph?.let {
            return it.findPath(sourceStationName, destStationName)?.map { (routeId, stopName) ->
                Pair(
                    getRouteFromId(routeId),
                    stopName
                )
            }
        }
        throw IllegalStateException("The route data has not been loaded yet.")
    }
}