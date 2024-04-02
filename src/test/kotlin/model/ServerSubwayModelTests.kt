package model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Includes tests for SubwayModel.kt that use true API calls to test
 * functionality. These tests may fail if the MBTA API data changes unexpectedly
 * in the future.
 */
class ServerSubwayModelTests {
    @Test
    fun testLoadAndAccessRouteData() {
        val model = MbtaSubwayModel()

        assertThrows<IllegalStateException> { model.getSubwayRoutes() }

        model.loadRouteData()

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
            model.getSubwayRoutes()
        )
    }

    @Test
    fun testGetSubwayRouteWithMostStops() {
        val model = MbtaSubwayModel()

        assertThrows<IllegalStateException> { model.getSubwayRouteWithMostStops() }

        model.loadRouteData()

        assertEquals(
            Pair(
                Route("Green-D", "Green Line D"),
                25
            ),
            model.getSubwayRouteWithMostStops()
        )
    }

    @Test
    fun testGetSubwayRouteWithFewestStops() {
        val model = MbtaSubwayModel()

        assertThrows<IllegalStateException> { model.getSubwayRouteWithFewestStops() }

        model.loadRouteData()

        assertEquals(
            Pair(
                Route("Mattapan", "Mattapan Trolley"),
                8
            ),
            model.getSubwayRouteWithFewestStops()
        )
    }

    @Test
    fun testGetTransferStops() {
        val model = MbtaSubwayModel()

        assertThrows<IllegalStateException> { model.getTransferStops() }

        model.loadRouteData()

        val transferStopsMap = model.getTransferStops()

        assertFalse(transferStopsMap.containsKey("Northeastern University"))

        assertEquals(
            listOf(
                Route("Red", "Red Line"),
                Route("Green-B", "Green Line B"),
                Route("Green-C", "Green Line C"),
                Route("Green-D", "Green Line D"),
                Route("Green-E", "Green Line E")
            ),
            transferStopsMap["Park Street"]
        )
    }

    @Test
    fun testFindPath() {
        val model = MbtaSubwayModel()

        assertThrows<IllegalStateException> {
            model.findPath("Downtown Crossing", "Ashmont")
        }

        model.loadRouteData()

        assertThrows<IllegalArgumentException> {
            model.findPath("Fake Stop", "Downtown Crossing")
        }

        assertThrows<IllegalArgumentException> {
            model.findPath("Downtown Crossing", "Fake Stop")
        }

        assertThrows<IllegalArgumentException> {
            model.findPath("Fake Stop", "Fake Stop")
        }

        assertNull(
            model.findPath("Ruggles", "Ruggles")
        )

        assertEquals(
            listOf(
                Pair(
                    Route("Orange", "Orange Line"), "Massachusetts Avenue"
                )
            ),
            model.findPath("Ruggles", "Massachusetts Avenue")
        )

        assertEquals(
            listOf(
                Pair(
                    Route("Red", "Red Line"), "Broadway"
                ),
                Pair(
                    Route("Red", "Red Line"), "South Station"
                )
            ),
            model.findPath("Andrew", "South Station")
        )

        assertEquals(
            listOf(
                Pair(
                    Route("Orange", "Orange Line"), "Chinatown"
                ),
                Pair(
                    Route("Orange", "Orange Line"), "Downtown Crossing"
                ),
                Pair(
                    Route("Red", "Red Line"), "South Station"
                ),
                Pair(
                    Route("Red", "Red Line"), "Broadway"
                ),
                Pair(
                    Route("Red", "Red Line"), "Andrew"
                ),
                Pair(
                    Route("Red", "Red Line"), "JFK/UMass"
                ),
                Pair(
                    Route("Red", "Red Line"), "Savin Hill"
                ),
                Pair(
                    Route("Red", "Red Line"), "Fields Corner"
                ),
                Pair(
                    Route("Red", "Red Line"), "Shawmut"
                ),
                Pair(
                    Route("Red", "Red Line"), "Ashmont"
                ),
                Pair(
                    Route("Mattapan", "Mattapan Trolley"), "Cedar Grove"
                )
            ),
            model.findPath("Tufts Medical Center", "Cedar Grove")
        )

        // Transfers are not made if not immediately needed.
        assertEquals(
            listOf(
                Pair(
                    Route("Green-E", "Green Line E"), "Copley"
                ),
                Pair(
                    Route("Green-E", "Green Line E"), "Arlington"
                )
            ),
            model.findPath("Prudential", "Arlington")
        )
    }
}