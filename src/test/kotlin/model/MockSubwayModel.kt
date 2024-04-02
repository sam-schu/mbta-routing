package model

/**
 * Mock subway model that allows the model's route data to be directly
 * specified.
 *
 * @param routes the route data to be stored in the model.
 * @param dataLoaded whether the data should be initially considered to be
 * loaded, meaning it can be accessed without calling [loadRouteData] first.
 */
class MockSubwayModel(
    private val routes: List<Route>,
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
     * Gets the model's list of subway routes.
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

    override fun getSubwayRouteWithMostStops(): Pair<Route, Int>? {
        TODO("Not yet implemented")
    }

    override fun getSubwayRouteWithFewestStops(): Pair<Route, Int>? {
        TODO("Not yet implemented")
    }

    override fun getTransferStops(): Map<String, List<Route>> {
        TODO("Not yet implemented")
    }
}