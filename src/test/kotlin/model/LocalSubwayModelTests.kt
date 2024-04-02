package model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Includes tests for SubwayModel.kt that do not rely on true data from the MBTA
 * API to pass.
 */
class LocalSubwayModelTests {
    @Test
    fun testGetSubwayRoutesBeforeLoadingRouteData() {
        val model = MbtaSubwayModel()

        assertThrows<IllegalStateException> { model.getSubwayRoutes() }
    }

    @Test
    fun testLoadRouteDataSuccess() {
        testWithMockServer(
            emptyResponse, emptyResponse, singleRoute, emptyResponse, threeRoutes, emptyResponse
        ) { url ->
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))

            model.loadRouteData()

            assertEquals(
                listOf(),
                model.getSubwayRoutes()
            )

            model.loadRouteData()

            assertEquals(
                listOf(Route("Red", "Red Line")),
                model.getSubwayRoutes()
            )

            model.loadRouteData()

            // getSubwayRoutes does not consume the route data
            repeat(2) {
                assertEquals(
                    listOf(
                        Route("Red", "Red Line"),
                        Route("Mattapan", "Mattapan Trolley"),
                        Route("Orange", "Orange Line")
                    ),
                    model.getSubwayRoutes()
                )
            }
        }
    }

    @Test
    fun testLoadRouteDataFailure() {
        testWithMockServer(
            badRequest, forbidden, rateLimited, malformedResponse, singleRouteMissingLongName
        ) { url ->
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))

            repeat(5) {
                assertThrows<IOException> { model.loadRouteData() }
            }
        }
    }

    @Test
    fun testLoadRouteDataMixedSuccess() {
        testWithMockServer(
            malformedResponse, threeRoutes, emptyResponse, rateLimited, emptyResponse
        ) { url ->
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))
            val threeRoutesData = listOf(
                Route("Red", "Red Line"),
                Route("Mattapan", "Mattapan Trolley"),
                Route("Orange", "Orange Line")
            )

            assertThrows<IllegalStateException> { model.getSubwayRoutes() }

            // Malformed response
            assertThrows<IOException> { model.loadRouteData() }
            assertThrows<IllegalStateException> { model.getSubwayRoutes() }

            // Three routes
            model.loadRouteData()

            assertEquals(
                threeRoutesData,
                model.getSubwayRoutes()
            )

            // Rate limited
            assertThrows<IOException> { model.loadRouteData() }

            // If loading new data fails, the model still stores the previously
            // loaded data
            assertEquals(
                threeRoutesData,
                model.getSubwayRoutes()
            )
        }
    }

    @Test
    fun testGetSubwayRoutesWithMostAndFewestStopsNoRoutes() {
        testWithMockServer(emptyResponse, emptyResponse) { url ->
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))

            assertThrows<IllegalStateException> { model.getSubwayRouteWithMostStops() }
            assertThrows<IllegalStateException> { model.getSubwayRouteWithFewestStops() }

            model.loadRouteData()

            assertNull(model.getSubwayRouteWithMostStops())
            assertNull(model.getSubwayRouteWithFewestStops())
        }
    }

    @Test
    fun testGetSubwayRoutesWithMostAndFewestStopsThreeRoutes() {
        testWithMockServer(threeRoutes, threeRoutesRoutePatterns) { url ->
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))

            assertThrows<IllegalStateException> { model.getSubwayRouteWithMostStops() }
            assertThrows<IllegalStateException> { model.getSubwayRouteWithFewestStops() }

            model.loadRouteData()

            assertEquals(
                Pair(
                    Route("Red", "Red Line"),
                    22
                ),
                model.getSubwayRouteWithMostStops()
            )

            assertEquals(
                Pair(
                    Route("Mattapan", "Mattapan Trolley"),
                    8
                ),
                model.getSubwayRouteWithFewestStops()
            )
        }
    }

    @Test
    fun testGetTransferStopsNoRoutes() {
        testWithMockServer(emptyResponse, emptyResponse) { url ->
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))

            assertThrows<IllegalStateException> { model.getTransferStops() }

            model.loadRouteData()

            assertEquals(
                mapOf(),
                model.getTransferStops()
            )
        }
    }

    @Test
    fun testGetTransferStopsThreeRoutes() {
        testWithMockServer(threeRoutes, threeRoutesRoutePatterns) { url ->
            val model = MbtaSubwayModel(MbtaApiCaller(MbtaApi(url)))

            assertThrows<IllegalStateException> { model.getTransferStops() }

            model.loadRouteData()

            assertEquals(
                mapOf(
                    Pair("Downtown Crossing", listOf(
                        Route("Red", "Red Line"),
                        Route("Orange", "Orange Line")
                    )),
                    Pair("Ashmont", listOf(
                        Route("Red", "Red Line"),
                        Route("Mattapan", "Mattapan Trolley")
                    ))
                ),
                model.getTransferStops()
            )
        }
    }
}