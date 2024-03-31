package model

import okhttp3.mockwebserver.MockResponse

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
