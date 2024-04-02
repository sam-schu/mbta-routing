package model

import retrofit2.Call
import java.io.IOException

/**
 * Provides access to data from API calls with error handling.
 */
internal interface ApiCaller {
    /**
     * Returns all subway routes (routes of the Light Rail or Heavy Rail type).
     *
     * @throws IOException if any server- or parsing-related error occurs that
     * prevents a valid list of Routes from being obtained.
     */
    fun getSubwayRoutes(): List<Route>

    /**
     * Returns all canonical route patterns associated with the given list of
     * routes (based on their IDs).
     *
     * Canonical route patterns associated with replacement shuttles for the
     * provided routes will not be included (unless the replacement shuttles
     * themselves are included in the given list of routes).
     *
     * @throws IOException if any server- or parsing-related error occurs that
     * prevents a valid list of RoutePatterns from being obtained.
     */
    fun getCanonicalRoutePatterns(routes: List<Route>): List<RoutePattern>
}

/**
 * Provides access to MBTA data from a particular Api while consolidating all
 * server- or parsing-related errors into an IOException.
 *
 * @constructor Creates the API service used to make the API calls from the
 * specified Api.
 */
internal class MbtaApiCaller(api: Api = MbtaApi()) : ApiCaller {
    private val apiService = api.createService()

    override fun getSubwayRoutes(): List<Route> {
        return performCall(apiService.getSubwayRoutes())
    }

    override fun getCanonicalRoutePatterns(routes: List<Route>): List<RoutePattern> {
        val routeIds = routes.map { it.id }
        val filterString = routeIds.joinToString(separator = ",")
        val routePatterns = performCall(apiService.getCanonicalRoutePatterns(filterString))

        // Removes unwanted replacement shuttle route patterns.
        return routePatterns.filter {
            if (it.route == null) {
                false
            } else {
                it.route.id in routeIds
            }
        }
    }

    // Synchronously executes the given call; returns the body if the call was
    // successful, or throws an IOException if the data could not be obtained or
    // parsed correctly.
    private fun <T> performCall(call: Call<T>): T {
        try {
            val response = call.execute()

            if (response.isSuccessful) {
                return response.body() ?: throw IOException(
                    "The body of the HTTP response obtained was null."
                )
            } else {
                throw IOException(
                    "An unsuccessful HTTP response was obtained:\n"
                            + "Error code: ${response.code()}\n"
                            + "Error body: ${response.errorBody()}"
                )
            }
        } catch (e: RuntimeException) {
            handleCallException(e)
        } catch (e: IOException) {
            handleCallException(e)
        }
    }

    // Throws an IOException caused by the given exception produced while
    // executing a call.
    private fun handleCallException(e: Exception): Nothing {
        throw IOException(
            "An error occurred while obtaining or parsing an API response.",
            e
        )
    }
}