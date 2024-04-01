package model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

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
}