package model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals

/**
 * Includes tests for ApiCaller.kt that do not rely on true data from the MBTA
 * API to pass.
 */
class LocalApiCallerTests {
    @Test
    fun testGetSubwayRoutesSuccess() {
        testWithMockServer(emptyResponse, singleRoute, threeRoutes) { url ->
            val apiCaller = MbtaApiCaller(MbtaApi(url))

            assertEquals(
                listOf(),
                apiCaller.getSubwayRoutes()
            )

            assertEquals(
                listOf(Route("Red", "Red Line")),
                apiCaller.getSubwayRoutes()
            )

            assertEquals(
                listOf(
                    Route("Red", "Red Line"),
                    Route("Mattapan", "Mattapan Trolley"),
                    Route("Orange", "Orange Line")
                ),
                apiCaller.getSubwayRoutes()
            )
        }
    }

    @Test
    fun testGetSubwayRoutesFailure() {
        testWithMockServer(
            badRequest, forbidden, rateLimited, malformedResponse, singleRouteMissingLongName
        ) { url ->
            val apiCaller = MbtaApiCaller(MbtaApi(url))

            repeat(5) {
                assertThrows<IOException> { apiCaller.getSubwayRoutes() }
            }
        }
    }

    @Test
    fun testGetSubwayRoutesMixedSuccess() {
        testWithMockServer(rateLimited, threeRoutes) { url ->
            val apiCaller = MbtaApiCaller(MbtaApi(url))

            assertThrows<IOException> { apiCaller.getSubwayRoutes() }

            assertEquals(
                listOf(
                    Route("Red", "Red Line"),
                    Route("Mattapan", "Mattapan Trolley"),
                    Route("Orange", "Orange Line")
                ),
                apiCaller.getSubwayRoutes()
            )
        }
    }
}