package model

/**
 * Mock subway model that allows the model's outputs to be directly specified.
 *
 * @param routes the route data to be outputted from [getSubwayRoutes].
 * @param subwayRouteWithMostStops the result to be outputted from
 * [getSubwayRouteWithMostStops].
 * @param subwayRouteWithFewestStops the result to be outputted from
 * [getSubwayRouteWithFewestStops].
 * @param transferStops the result to be outputted from [getTransferStops].
 * @param path the result to be outputted from [findPath] (regardless of the
 * inputs).
 * @param dataLoaded whether the data should be initially considered to be
 * loaded, meaning it can be accessed without an IllegalStateException without
 * needing to call [loadRouteData] first.
 */
class MockSubwayModel(
    private val routes: List<Route> = listOf(),
    private val subwayRouteWithMostStops: Pair<Route, Int>? = null,
    private val subwayRouteWithFewestStops: Pair<Route, Int>? = null,
    private val transferStops: Map<String, List<Route>> = mapOf(),
    private val path: List<Pair<Route, String>>? = null,
    private var dataLoaded: Boolean = false
) : MutableSubwayModel {
    /**
     * Allows model data to be accessed without throwing an
     * IllegalStateException. Does not affect the stored route data.
     */
    override fun loadRouteData() {
        dataLoaded = true
    }

    /**
     * Returns the value specified in the constructor.
     *
     * @throws IllegalStateException if the model was not created with the route
     * data initially loaded, and the [loadRouteData] method has not yet been
     * called.
     */
    override fun getSubwayRoutes(): List<Route> {
        if (!dataLoaded) {
            throw IllegalStateException("The route data has not been loaded yet.")
        }
        return routes
    }

    /**
     * Returns the value specified in the constructor.
     *
     * @throws IllegalStateException if the model was not created with the route
     * data initially loaded, and the [loadRouteData] method has not yet been
     * called.
     */
    override fun getSubwayRouteWithMostStops(): Pair<Route, Int>? {
        if (!dataLoaded) {
            throw IllegalStateException("The route data has not been loaded yet.")
        }
        return subwayRouteWithMostStops
    }

    /**
     * Returns the value specified in the constructor.
     *
     * @throws IllegalStateException if the model was not created with the route
     * data initially loaded, and the [loadRouteData] method has not yet been
     * called.
     */
    override fun getSubwayRouteWithFewestStops(): Pair<Route, Int>? {
        if (!dataLoaded) {
            throw IllegalStateException("The route data has not been loaded yet.")
        }
        return subwayRouteWithFewestStops
    }

    /**
     * Returns the value specified in the constructor.
     *
     * @throws IllegalStateException if the model was not created with the route
     * data initially loaded, and the [loadRouteData] method has not yet been
     * called.
     */
    override fun getTransferStops(): Map<String, List<Route>> {
        if (!dataLoaded) {
            throw IllegalStateException("The route data has not been loaded yet.")
        }
        return transferStops
    }

    /**
     * Returns the value specified in the constructor.
     *
     * @throws IllegalStateException if the model was not created with the route
     * data initially loaded, and the [loadRouteData] method has not yet been
     * called.
     */
    override fun findPath(
        sourceStationName: String,
        destStationName: String
    ): List<Pair<Route, String>>? {
        if (!dataLoaded) {
            throw IllegalStateException("The route data has not been loaded yet.")
        }
        return path
    }
}