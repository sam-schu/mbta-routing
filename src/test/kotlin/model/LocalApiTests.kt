package model

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
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

        assertEquals(Api.MBTA_API_BASE_URL, Api().retrofit.baseUrl().toString())
        assertEquals(validUrl, Api(validUrl).retrofit.baseUrl().toString())
        assertThrows<IllegalArgumentException> { Api(invalidUrl) }
    }

    @Test
    fun testGetSubwayRoutesSuccess() {
        testWithMockServer(emptyResponse, singleRoute, threeRoutes) { url ->
            val service = Api(url).createService()

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
        ) {url ->
            val service = Api(url).createService()

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
        testWithMockServer(rateLimited, threeRoutes) {url ->
            val service = Api(url).createService()

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

    // Enqueues the provided responses onto a mock local web server, and then
    // runs the specified test with the string URL of the server as an argument
    private fun testWithMockServer(
        vararg responses: MockResponse,
        test: (String) -> Unit
    ) {
        val mockServer = MockWebServer().apply {
            responses.forEach {
                enqueue(it)
            }
            start()
        }

        test(mockServer.url("/").toString())

        mockServer.shutdown()
    }
}