package model

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

// Enqueues the provided responses onto a mock local web server, and then
// runs the specified test with the string URL of the server as an argument.
fun testWithMockServer(
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



// MOCK SERVER RESPONSES



// General responses

val badRequest = MockResponse().setResponseCode(400).setBody(
    "{\"errors\":[{\"code\":\"bad_request\",\"detail\":\"Unsupported filter(s): hello\",\"source\":{\"parameter\":\"filter\"},\"status\":\"400\"}],\"jsonapi\":{\"version\":\"1.0\"}}"
)

val forbidden = MockResponse().setResponseCode(403).setBody(
    "{\"errors\":[{\"code\":\"forbidden\",\"status\":\"403\"}],\"jsonapi\":{\"version\":\"1.0\"}}"
)

val rateLimited = MockResponse().setResponseCode(429).setBody(
    "{\"errors\":[{\"code\":\"rate_limited\",\"detail\":\"You have exceeded your allowed usage rate.\",\"status\":\"429\"}],\"jsonapi\":{\"version\":\"1.0\"}}"
)

val malformedResponse = MockResponse().setBody(
    "malformed"
)

val emptyResponse = MockResponse().setBody(
    "{\"data\":[],\"jsonapi\":{\"version\":\"1.0\"}}"
)



// routes endpoint responses

val singleRouteMissingLongName = MockResponse().setBody(
    "{\"data\":[{\"attributes\":{},\"id\":\"Red\",\"links\":{\"self\":\"/routes/Red\"},\"relationships\":{\"line\":{\"data\":{\"id\":\"line-Red\",\"type\":\"line\"}}},\"type\":\"route\"}],\"jsonapi\":{\"version\":\"1.0\"}}"
)

val singleRoute = MockResponse().setBody(
    "{\"data\":[{\"attributes\":{\"long_name\":\"Red Line\"},\"id\":\"Red\",\"links\":{\"self\":\"/routes/Red\"},\"relationships\":{\"line\":{\"data\":{\"id\":\"line-Red\",\"type\":\"line\"}}},\"type\":\"route\"}],\"jsonapi\":{\"version\":\"1.0\"}}"
)

val threeRoutes = MockResponse().setBody(
    "{\"data\":[{\"attributes\":{\"long_name\":\"Red Line\"},\"id\":\"Red\",\"links\":{\"self\":\"/routes/Red\"},\"relationships\":{\"line\":{\"data\":{\"id\":\"line-Red\",\"type\":\"line\"}}},\"type\":\"route\"},{\"attributes\":{\"long_name\":\"Mattapan Trolley\"},\"id\":\"Mattapan\",\"links\":{\"self\":\"/routes/Mattapan\"},\"relationships\":{\"line\":{\"data\":{\"id\":\"line-Mattapan\",\"type\":\"line\"}}},\"type\":\"route\"},{\"attributes\":{\"long_name\":\"Orange Line\"},\"id\":\"Orange\",\"links\":{\"self\":\"/routes/Orange\"},\"relationships\":{\"line\":{\"data\":{\"id\":\"line-Orange\",\"type\":\"line\"}}},\"type\":\"route\"}],\"jsonapi\":{\"version\":\"1.0\"}}"
)



// route_patterns endpoint responses

val greenLineRoutePattern = MockResponse().setBody(
    "{\"data\":[{\"attributes\":{},\"id\":\"Green-E-886-0\",\"links\":{\"self\":\"/route_patterns/Green-E-886-0\"},\"relationships\":{\"representative_trip\":{\"data\":{\"id\":\"canonical-Green-E-C1-0\",\"type\":\"trip\"}},\"route\":{\"data\":{\"id\":\"Green-E\",\"type\":\"route\"}}},\"type\":\"route_pattern\"}],\"included\":[{\"attributes\":{\"name\":\"Symphony\"},\"id\":\"70241\",\"links\":{\"self\":\"/stops/70241\"},\"relationships\":{\"facilities\":{\"links\":{\"related\":\"/facilities/?filter[stop]=70241\"}},\"parent_station\":{\"data\":{\"id\":\"place-symcl\",\"type\":\"stop\"}},\"zone\":{\"data\":{\"id\":\"RapidTransit\",\"type\":\"zone\"}}},\"type\":\"stop\"},{\"attributes\":{\"name\":\"Symphony\"},\"id\":\"place-symcl\",\"links\":{\"self\":\"/stops/place-symcl\"},\"relationships\":{\"facilities\":{\"links\":{\"related\":\"/facilities/?filter[stop]=place-symcl\"}},\"parent_station\":{\"data\":null},\"zone\":{\"data\":null}},\"type\":\"stop\"},{\"attributes\":{},\"id\":\"canonical-Green-E-C1-0\",\"links\":{\"self\":\"/trips/canonical-Green-E-C1-0\"},\"relationships\":{\"route\":{\"data\":{\"id\":\"Green-E\",\"type\":\"route\"}},\"route_pattern\":{\"data\":{\"id\":\"Green-E-886-0\",\"type\":\"route_pattern\"}},\"service\":{\"data\":{\"id\":\"canonical\",\"type\":\"service\"}},\"shape\":{\"data\":{\"id\":\"canonical-8000018\",\"type\":\"shape\"}},\"stops\":{\"data\":[{\"id\":\"70241\",\"type\":\"stop\"}]}},\"type\":\"trip\"}],\"jsonapi\":{\"version\":\"1.0\"}}"
)