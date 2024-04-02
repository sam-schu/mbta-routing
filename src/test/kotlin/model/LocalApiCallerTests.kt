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

    @Test
    fun testGetCanonicalRoutePatternsSuccess() {
        testWithMockServer(emptyResponse, greenLineRoutePattern) { url ->
            val apiCaller = MbtaApiCaller(MbtaApi(url))

            assertEquals(
                listOf(),
                apiCaller.getCanonicalRoutePatterns(listOf(Route("Green-E", "Green Line E")))
            )

            assertEquals(
                listOf(RoutePattern(
                    "Green-E-886-0",
                    RouteId("Green-E"),
                    Trip(
                        "canonical-Green-E-C1-0",
                        listOf(Stop(
                            "70241",
                            "Symphony",
                            Stop(
                                "place-symcl",
                                "Symphony",
                                null
                            )
                        ))
                    )
                )),
                apiCaller.getCanonicalRoutePatterns(listOf(Route("Green-E", "Green Line E")))
            )
        }
    }

    @Test
    fun testGetCanonicalRoutePatternsFailure() {
        testWithMockServer(
            badRequest, forbidden, rateLimited, malformedResponse, singleRouteMissingLongName
        ) { url ->
            val apiCaller = MbtaApiCaller(MbtaApi(url))

            repeat(5) {
                assertThrows<IOException> {
                    apiCaller.getCanonicalRoutePatterns(listOf(Route("Green-E", "Green Line E")))
                }
            }
        }
    }

    @Test
    fun testGetCanonicalRoutePatternsMixedSuccess() {
        testWithMockServer(rateLimited, greenLineRoutePattern) { url ->
            val apiCaller = MbtaApiCaller(MbtaApi(url))

            assertThrows<IOException> {
                apiCaller.getCanonicalRoutePatterns(listOf(Route("Green-E", "Green Line E")))
            }

            assertEquals(
                listOf(RoutePattern(
                    "Green-E-886-0",
                    RouteId("Green-E"),
                    Trip(
                        "canonical-Green-E-C1-0",
                        listOf(Stop(
                            "70241",
                            "Symphony",
                            Stop(
                                "place-symcl",
                                "Symphony",
                                null
                            )
                        ))
                    )
                )),
                apiCaller.getCanonicalRoutePatterns(listOf(Route("Green-E", "Green Line E")))
            )
        }
    }

    @Test
    fun testGetCanonicalRoutePatternsFiltering() {
        testWithMockServer(greenLineRoutePattern) { url ->
            val apiCaller = MbtaApiCaller(MbtaApi(url))

            assertEquals(
                listOf(),
                apiCaller.getCanonicalRoutePatterns(listOf(Route("Green-D", "Green Line D")))
            )
        }
    }
}