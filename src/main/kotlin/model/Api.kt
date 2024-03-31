package model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

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
            Route::class.java
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
    @GET(MbtaApi.SUBWAY_ROUTES_ENDPOINT)
    fun getSubwayRoutes(): Call<List<Route>>
}