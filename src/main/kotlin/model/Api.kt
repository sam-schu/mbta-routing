package model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.jasminb.jsonapi.annotations.Id
import com.github.jasminb.jsonapi.annotations.Type
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

/**
 * Provides access to the MBTA API through a Retrofit API service.
 *
 * @constructor Creates the Retrofit instance using the provided base URL.
 * @throws IllegalArgumentException if the provided base URL is not a valid URL.
 */
internal class Api(baseUrl: String = MBTA_API_BASE_URL) {
    /**
     * Retrofit instance that can be used to create a service to obtain data
     * from the MBTA API.
     */
    internal val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JSONAPIConverterFactory(
            ObjectMapper().registerKotlinModule(),
            Route::class.java
        ))
        .build()

    /**
     * Returns a newly created ApiService from the Api's Retrofit instance that
     * provides access to API calls to obtain MBTA data.
     */
    internal fun createService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    internal companion object {
        /**
         * Base URL for the MBTA API. Not included in individual endpoint URLs.
         */
        internal const val MBTA_API_BASE_URL =
            "https://api-v3.mbta.com/"

        /**
         * Endpoint to obtain a list of all subway routes (routes of the Light
         * Rail or Heavy Rail type).
         */
        internal const val SUBWAY_ROUTES_ENDPOINT =
            "routes?filter[type]=0,1&fields[route]=long_name"
    }
}

/**
 * Retrofit API service providing API calls for specific endpoints of the MBTA
 * API that can be executed to obtain data from the API.
 */
internal interface ApiService {
    /**
     * Returns a call to obtain a list of all subway routes (routes of the Light
     * Rail or Heavy Rail type) from the API.
     */
    @GET(Api.SUBWAY_ROUTES_ENDPOINT)
    fun getSubwayRoutes(): Call<List<Route>>
}

/**
 * A single route on the MBTA system.
 *
 * A route represents what is typically thought of as a "line" - e.g., the Red
 * Line, or a single numbered bus route. A route does not necessarily represent
 * a single sequence of stops, but rather a group of such "route patterns" that
 * are all described under the same name.
 *
 * @property id the ID of the route from the API.
 * @property name the name of the route, corresponding to its long_name from the
 * API.
 */
@Type("route")
data class Route(
    @Id val id: String?,
    @JsonProperty("long_name") val name: String
)