package model.graph

import model.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * Includes tests for TransitGraph.kt. These tests do not rely on data from the
 * true MBTA API.
 */
class TransitGraphTests {
    @Test
    fun testFromRoutePatternsEmpty() {
        val graph = MbtaTransitGraph.fromRoutePatterns(listOf())

        assertEquals(mutableListOf(), graph.stationNodes)
    }

    @Test
    fun testFromRoutePatternsSingleStop() {
        val graph = MbtaTransitGraph.fromRoutePatterns(
            listOf(
                RoutePattern(
                "Green-E-886-0",
                RouteId("Green-E"),
                Trip(
                    "canonical-Green-E-C1-0",
                    listOf(
                        Stop(
                        "70241",
                        "Symphony",
                        Stop(
                            "place-symcl",
                            "Symphony",
                            null
                        )
                    )
                    )
                )
            )
            )
        )

        val expectedNodes = mutableListOf(
            StationNode(
                "place-symcl",
                "Symphony",
                mutableSetOf("Green-E"),
                hashMapOf()
            )
        )

        assertEquals(expectedNodes, graph.stationNodes)
    }

    @Test
    fun testFromRoutePatternsThreeStops() {
        val graph = MbtaTransitGraph.fromRoutePatterns(
            listOf(
                RoutePattern(
                    "Green-E-886-0",
                    RouteId("Green-E"),
                    Trip(
                        "canonical-Green-E-C1-0",
                        listOf(
                            Stop(
                                "70241",
                                "Symphony",
                                Stop(
                                    "place-symcl",
                                    "Symphony",
                                    null
                                )
                            ),
                            Stop(
                                "70242",
                                "Prudential",
                                Stop(
                                    "prudential",
                                    "Prudential",
                                    null
                                )
                            ),
                            Stop(
                                "70243",
                                "Copley",
                                Stop(
                                    "copley",
                                    "Copley",
                                    null
                                )
                            )
                        )
                    )
                )
            )
        )

        val expectedNodes = mutableListOf(
            StationNode(
                "place-symcl",
                "Symphony",
                mutableSetOf("Green-E"),
                hashMapOf()
            ),
            StationNode(
                "prudential",
                "Prudential",
                mutableSetOf("Green-E"),
                hashMapOf()
            ),
            StationNode(
                "copley",
                "Copley",
                mutableSetOf("Green-E"),
                hashMapOf()
            )
        )

        expectedNodes[0].outgoingEdges["prudential"] = Edge(
            expectedNodes[0],
            expectedNodes[1],
            mutableSetOf("Green-E")
        )
        expectedNodes[1].outgoingEdges["copley"] = Edge(
            expectedNodes[1],
            expectedNodes[2],
            mutableSetOf("Green-E")
        )

        assertEquals(expectedNodes, graph.stationNodes)
    }

    @Test
    fun testFromRoutePatternsMerging() {
        val graph = MbtaTransitGraph.fromRoutePatterns(
            listOf(
                RoutePattern(
                    "Red",
                    RouteId("Red"),
                    Trip(
                        "red",
                        listOf(
                            Stop(
                                "1",
                                "A",
                                Stop(
                                    "1",
                                    "A",
                                    null
                                )
                            ),
                            Stop(
                                "2",
                                "B",
                                Stop(
                                    "2",
                                    "B",
                                    null
                                )
                            )
                        )
                    )
                ),
                RoutePattern(
                    "Red",
                    RouteId("Red"),
                    Trip(
                        "red2",
                        listOf(
                            Stop(
                                "2",
                                "B",
                                Stop(
                                    "2",
                                    "B",
                                    null
                                )
                            ),
                            Stop(
                                "1",
                                "A",
                                Stop(
                                    "1",
                                    "A",
                                    null
                                )
                            )
                        )
                    )
                ),
                RoutePattern(
                    "Blue",
                    RouteId("Blue"),
                    Trip(
                        "blue",
                        listOf(
                            Stop(
                                "1",
                                "A",
                                Stop(
                                    "1",
                                    "A",
                                    null
                                )
                            ),
                            Stop(
                                "2",
                                "B",
                                Stop(
                                    "2",
                                    "B",
                                    null
                                )
                            ),
                            Stop(
                                "3",
                                "C",
                                Stop(
                                    "3",
                                    "C",
                                    null
                                )
                            )
                        )
                    )
                )
            )
        )

        val expectedNodes = mutableListOf(
            StationNode(
                "1",
                "A",
                mutableSetOf("Red", "Blue"),
                hashMapOf()
            ),
            StationNode(
                "2",
                "B",
                mutableSetOf("Red", "Blue"),
                hashMapOf()
            ),
            StationNode(
                "3",
                "C",
                mutableSetOf("Blue"),
                hashMapOf()
            )
        )

        expectedNodes[0].outgoingEdges["2"] = Edge(
            expectedNodes[0],
            expectedNodes[1],
            mutableSetOf("Red", "Blue")
        )
        expectedNodes[1].outgoingEdges["1"] = Edge(
            expectedNodes[1],
            expectedNodes[0],
            mutableSetOf("Red")
        )
        expectedNodes[1].outgoingEdges["3"] = Edge(
            expectedNodes[1],
            expectedNodes[2],
            mutableSetOf("Blue")
        )

        assertEquals(expectedNodes, graph.stationNodes)
    }
}