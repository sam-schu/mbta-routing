package model

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
}

/**
 * Enables MBTA subway data to be loaded (using an API caller), stored, and
 * accessed.
 */
class MbtaSubwayModel : MutableSubwayModel {
    private val apiCaller: ApiCaller
    private var routes: List<Route>? = null

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
        routes = apiCaller.getSubwayRoutes()
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
}