package model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Provides access to a Retrofit API service with API calls for route and stop
 * data.
 */
internal interface Api {
    /**
     * Returns a newly created ApiService that can be used to obtain route and
     * stop data.
     */
    fun createService(): ApiService
}

/**
 * Provides access to the MBTA API through a Retrofit API service.
 *
 * @constructor Creates the Retrofit instance using the provided base URL.
 * @throws IllegalArgumentException if the provided base URL is not a valid URL.
 */
internal class MbtaApi(baseUrl: String = MBTA_API_BASE_URL) : Api {
    /**
     * Retrofit instance that can be used to create a service to obtain data
     * from the MBTA API.
     */
    internal val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JSONAPIConverterFactory(
            ObjectMapper().registerKotlinModule(),
            Route::class.java, RoutePattern::class.java, RouteId::class.java,
            Trip::class.java, Stop::class.java
        ))
        .build()

    override fun createService(): ApiService {
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

        /**
         * Endpoint to obtain a list of all canonical route patterns and
         * associated information.
         */
        internal const val CANONICAL_ROUTE_PATTERNS_ENDPOINT =
            "route_patterns?filter[canonical]=true&include=representative_trip.stops.parent_station&fields[route_pattern]=&fields[trip]=&fields[stop]=name"
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
     *
     * Implementation note: Filtering is performed using the server API rather
     * than locally to avoid downloading significant amounts of unnecessary data
     * from the server, which could waste network bandwidth and potentially
     * degrade the speed of the application (due to a slow download speed and/or
     * the time required for the additional local filtering work).
     */
    @GET(MbtaApi.SUBWAY_ROUTES_ENDPOINT)
    fun getSubwayRoutes(): Call<List<Route>>

    /**
     * Returns a call to obtain a list of all canonical route patterns of a
     * certain set of routes from the API.
     *
     * Each canonical route pattern includes a representative trip that includes
     * the ordered list of stops that may typically be transited through along
     * a route.
     *
     * Route patterns associated with replacement shuttles for the given routes
     * may be included.
     *
     * @param routes the set of routes whose canonical route patterns should be
     * obtained. Must be formatted as a set of route IDs separated only by
     * commas (e.g., "Red,Blue").
     */
    @GET(MbtaApi.CANONICAL_ROUTE_PATTERNS_ENDPOINT)
    fun getCanonicalRoutePatterns(@Query("filter[route]") routes: String): Call<List<RoutePattern>>
}