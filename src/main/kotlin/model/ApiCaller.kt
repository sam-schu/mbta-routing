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