package model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Includes tests for Api.kt that do not rely on true data from the MBTA API to
 * pass.
 */
class LocalApiTests {
    @Test
    fun testApiInitialization() {
        val validUrl = "https://www.google.com/"
        val invalidUrl = "invalidURL"

        assertEquals(MbtaApi.MBTA_API_BASE_URL, MbtaApi().retrofit.baseUrl().toString())
        assertEquals(validUrl, MbtaApi(validUrl).retrofit.baseUrl().toString())
        assertThrows<IllegalArgumentException> { MbtaApi(invalidUrl) }
    }

    @Test
    fun testGetSubwayRoutesSuccess() {
        testWithMockServer(emptyResponse, singleRoute, threeRoutes) { url ->
            val service = MbtaApi(url).createService()

            assertEquals(
                listOf(),
                service.getSubwayRoutes().execute().body()
            )

            assertEquals(
                listOf(Route("Red", "Red Line")),
                service.getSubwayRoutes().execute().body()
            )

            assertEquals(
                listOf(
                    Route("Red", "Red Line"),
                    Route("Mattapan", "Mattapan Trolley"),
                    Route("Orange", "Orange Line")
                ),
                service.getSubwayRoutes().execute().body()
            )
        }
    }

    @Test
    fun testGetSubwayRoutesFailure() {
        testWithMockServer(
            badRequest, forbidden, rateLimited, malformedResponse, singleRouteMissingLongName
        ) { url ->
            val service = MbtaApi(url).createService()

            repeat(3) {
                assertEquals(false, service.getSubwayRoutes().execute().isSuccessful)
            }

            repeat(2) {
                assertThrows<RuntimeException> { service.getSubwayRoutes().execute() }
            }
        }
    }

    @Test
    fun testGetSubwayRoutesMixedSuccess() {
        testWithMockServer(rateLimited, threeRoutes) { url ->
            val service = MbtaApi(url).createService()

            assertEquals(false, service.getSubwayRoutes().execute().isSuccessful)

            assertEquals(
                listOf(
                    Route("Red", "Red Line"),
                    Route("Mattapan", "Mattapan Trolley"),
                    Route("Orange", "Orange Line")
                ),
                service.getSubwayRoutes().execute().body()
            )
        }
    }

    @Test
    fun testGetCanonicalRoutePatternsSuccess() {
        testWithMockServer(emptyResponse, greenLineRoutePattern) { url ->
            val service = MbtaApi(url).createService()

            assertEquals(
                listOf(),
                service.getCanonicalRoutePatterns("Green-E").execute().body()
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
                service.getCanonicalRoutePatterns("Green-E").execute().body()
            )
        }
    }

    @Test
    fun testGetCanonicalRoutePatternsFailure() {
        testWithMockServer(
            badRequest, forbidden, rateLimited, malformedResponse, singleRouteMissingLongName
        ) { url ->
            val service = MbtaApi(url).createService()

            repeat(3) {
                assertEquals(
                    false,
                    service.getCanonicalRoutePatterns("Green-E").execute().isSuccessful
                )
            }

            repeat(2) {
                assertThrows<RuntimeException> {
                    service.getCanonicalRoutePatterns("Green-E").execute()
                }
            }
        }
    }

    @Test
    fun testGetCanonicalRoutePatternsMixedSuccess() {
        testWithMockServer(rateLimited, greenLineRoutePattern) { url ->
            val service = MbtaApi(url).createService()

            assertEquals(
                false,
                service.getCanonicalRoutePatterns("Green-E").execute().isSuccessful
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
                service.getCanonicalRoutePatterns("Green-E").execute().body()
            )
        }
    }
}