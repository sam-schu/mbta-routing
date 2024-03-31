package model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Includes tests for ApiCaller.kt that use true API calls to test
 * functionality. These tests may fail if the MBTA API data changes unexpectedly
 * in the future.
 */
class ServerApiCallerTests {
    @Test
    fun testGetSubwayRoutes() {
        val apiCaller = MbtaApiCaller()

        assertEquals(
            listOf(
                Route("Red", "Red Line"),
                Route("Mattapan", "Mattapan Trolley"),
                Route("Orange", "Orange Line"),
                Route("Green-B", "Green Line B"),
                Route("Green-C", "Green Line C"),
                Route("Green-D", "Green Line D"),
                Route("Green-E", "Green Line E"),
                Route("Blue", "Blue Line")
            ),
            apiCaller.getSubwayRoutes()
        )
    }
}